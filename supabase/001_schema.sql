-- Passage Map: run once on a NEW, EMPTY Supabase project in SQL Editor.
-- Auth identities (email/password) live in Supabase Auth, never in public tables.

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  username text not null unique,
  visible boolean not null default false,
  created_at timestamptz not null default now(),
  constraint username_format check (username ~ '^[A-Za-z][A-Za-z0-9_]{2,23}$')
);

-- Create the profile automatically when Supabase Auth creates a user.
-- Enforce unique usernames in the database, not only in Swing.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles(id, username)
  values (new.id, new.raw_user_meta_data ->> 'username');
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

create table public.friend_requests (
  sender uuid not null references public.profiles(id) on delete cascade,
  recipient uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (sender, recipient),
  check (sender <> recipient)
);

-- Enforce at most one pending request for each unordered pair (including races).
create unique index friend_requests_unique_pair on public.friend_requests
  (least(sender, recipient), greatest(sender, recipient));

create table public.friendships (
  user_a uuid not null references public.profiles(id) on delete cascade,
  user_b uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (user_a, user_b),
  check (user_a < user_b)
);

-- payload stores the shared models.Pin fields, without redefining the teammate's entity.
-- visibility is separate so it can support public/private/friends_only later.
create table public.pins (
  owner uuid not null references public.profiles(id) on delete cascade,
  pin_id bigint not null,
  payload jsonb not null,
  visibility text not null default 'private',
  created_at timestamptz not null default now(),
  primary key (owner, pin_id),
  check (visibility in ('public','private','friends_only')),
  check (jsonb_typeof(payload) = 'object')
);
create index pins_visibility_idx on public.pins(visibility);

alter table public.profiles enable row level security;
alter table public.friend_requests enable row level security;
alter table public.friendships enable row level security;
alter table public.pins enable row level security;

-- No direct writes to friendship and pin tables: write through the vetted RPCs below.
revoke all on public.profiles, public.friend_requests, public.friendships, public.pins
  from anon, authenticated;
grant select on public.profiles, public.friend_requests, public.friendships, public.pins
  to authenticated;
grant update (visible) on public.profiles to authenticated;

create policy profiles_read on public.profiles
  for select to authenticated using (true);
create policy profiles_change_own_visibility on public.profiles
  for update to authenticated
  using ((select auth.uid()) = id)
  with check ((select auth.uid()) = id);

create policy requests_participants_read on public.friend_requests
  for select to authenticated
  using ((select auth.uid()) = sender or (select auth.uid()) = recipient);

create policy friendships_participants_read on public.friendships
  for select to authenticated
  using ((select auth.uid()) = user_a or (select auth.uid()) = user_b);

create policy pins_authorized_read on public.pins
  for select to authenticated
  using (
    owner = (select auth.uid())
    or visibility = 'public'
    or (
      visibility = 'friends_only'
      and exists (
        select 1 from public.friendships f
        where (f.user_a = (select auth.uid()) and f.user_b = owner)
           or (f.user_b = (select auth.uid()) and f.user_a = owner)
      )
    )
  );

-- One atomic server-side operation per friend request; no one may spoof the sender.
create or replace function public.send_friend_request(target_username text)
returns void language plpgsql security definer set search_path = '' as $$
declare v_sender uuid := (select auth.uid()); v_recipient uuid;
begin
  if v_sender is null then raise exception 'Sign in first'; end if;
  select p.id into v_recipient from public.profiles p where p.username = target_username;
  if v_recipient is null or v_recipient = v_sender then
    raise exception 'Invalid recipient';
  end if;
  if exists (select 1 from public.friendships f
    where (f.user_a = least(v_sender,v_recipient) and
           f.user_b = greatest(v_sender,v_recipient))) then
    raise exception 'Already friends';
  end if;
  if exists (select 1 from public.friend_requests r
    where r.sender = v_recipient and r.recipient = v_sender) then
    raise exception 'They already sent you a request';
  end if;
  insert into public.friend_requests(sender, recipient) values (v_sender, v_recipient);
end; $$;

create or replace function public.accept_friend_request(sender_username text)
returns void language plpgsql security definer set search_path = '' as $$
declare v_me uuid := (select auth.uid()); v_sender uuid;
begin
  if v_me is null then raise exception 'Sign in first'; end if;
  select id into v_sender from public.profiles where username = sender_username;
  if v_sender is null then raise exception 'Unknown sender'; end if;
  delete from public.friend_requests
    where sender = v_sender and recipient = v_me;
  if not found then raise exception 'No pending friend request'; end if;
  insert into public.friendships(user_a,user_b)
    values (least(v_me,v_sender), greatest(v_me,v_sender))
    on conflict do nothing;
end; $$;

create or replace function public.reject_friend_request(sender_username text)
returns void language plpgsql security definer set search_path = '' as $$
declare v_me uuid := (select auth.uid()); v_sender uuid;
begin
  if v_me is null then raise exception 'Sign in first'; end if;
  select id into v_sender from public.profiles where username = sender_username;
  if v_sender is null then raise exception 'Unknown sender'; end if;
  delete from public.friend_requests
    where sender = v_sender and recipient = v_me;
  if not found then raise exception 'No pending friend request'; end if;
end; $$;

create or replace function public.remove_friend(friend_username text)
returns void language plpgsql security definer set search_path = '' as $$
declare v_me uuid := (select auth.uid()); v_friend uuid;
begin
  if v_me is null then raise exception 'Sign in first'; end if;
  select id into v_friend from public.profiles where username = friend_username;
  if v_friend is null then raise exception 'Unknown friend'; end if;
  delete from public.friendships
    where user_a = least(v_me,v_friend) and user_b = greatest(v_me,v_friend);
  if not found then raise exception 'Users are not friends'; end if;
end; $$;

-- Replace a signed-in user's complete pin list atomically.
-- For current models.Pin, visible=true becomes public and false becomes private.
-- Future models with an explicit 'visibility' string can use friends_only.
create or replace function public.replace_my_pins(pin_list jsonb)
returns void language plpgsql security definer set search_path = '' as $$
declare v_me uuid := (select auth.uid()); v_user text; v_pin jsonb; v_visibility text;
begin
  if v_me is null then raise exception 'Sign in first'; end if;
  if pin_list is null or jsonb_typeof(pin_list) <> 'array'
     or jsonb_array_length(pin_list) > 1000 then
    raise exception 'Invalid pin list';
  end if;
  select username into v_user from public.profiles where id = v_me;
  delete from public.pins where owner = v_me;
  for v_pin in select elem from jsonb_array_elements(pin_list) as t(elem) loop
    if jsonb_typeof(v_pin) <> 'object' or not (v_pin ? 'id') then
      raise exception 'Invalid pin record';
    end if;
    v_visibility := coalesce(v_pin ->> 'visibility',
      case when coalesce((v_pin ->> 'visible')::boolean, false) then 'public'
           else 'private' end);
    if v_visibility not in ('public','private','friends_only') then
      raise exception 'Invalid pin visibility';
    end if;
    insert into public.pins(owner,pin_id,payload,visibility)
    values (v_me, (v_pin ->> 'id')::bigint,
      jsonb_set(v_pin, '{owner}', to_jsonb(v_user)), v_visibility);
  end loop;
end; $$;

-- Privileged functions are narrowly scoped to authenticated callers.
revoke all on function public.handle_new_user() from public, anon, authenticated;
revoke all on function public.send_friend_request(text) from public, anon, authenticated;
revoke all on function public.accept_friend_request(text) from public, anon, authenticated;
revoke all on function public.reject_friend_request(text) from public, anon, authenticated;
revoke all on function public.remove_friend(text) from public, anon, authenticated;
revoke all on function public.replace_my_pins(jsonb) from public, anon, authenticated;
grant execute on function public.send_friend_request(text) to authenticated;
grant execute on function public.accept_friend_request(text) to authenticated;
grant execute on function public.reject_friend_request(text) to authenticated;
grant execute on function public.remove_friend(text) to authenticated;
grant execute on function public.replace_my_pins(jsonb) to authenticated;


-- === Atomic single-pin operations used by the current pin edit/delete use cases ===
-- These functions never accept a caller-supplied owner identity. Supabase Auth is authoritative.
create or replace function public.save_my_pin(pin_data jsonb)
returns void language plpgsql security definer set search_path = '' as $$
declare
  v_me uuid := (select auth.uid());
  v_username text;
  v_id bigint;
  v_visibility text;
  v_payload jsonb;
begin
  if v_me is null then raise exception 'Sign in first'; end if;
  if pin_data is null or jsonb_typeof(pin_data) <> 'object'
     or not (pin_data ? 'id') or pin_data ->> 'id' !~ '^-?[0-9]+$' then
    raise exception 'Invalid pin';
  end if;
  v_id := (pin_data ->> 'id')::bigint;
  select username into v_username from public.profiles where id = v_me;
  v_visibility := coalesce(pin_data ->> 'visibility',
    case when coalesce((pin_data ->> 'visible')::boolean, false)
      then 'public' else 'private' end);
  if v_visibility not in ('public', 'private', 'friends_only') then
    raise exception 'Invalid audience';
  end if;
  v_payload := jsonb_set(pin_data, '{owner}', to_jsonb(v_username));
  insert into public.pins(owner, pin_id, payload, visibility)
  values (v_me, v_id, v_payload, v_visibility)
  on conflict (owner, pin_id)
    do update set payload = excluded.payload, visibility = excluded.visibility;
end; $$;

create or replace function public.edit_my_pin(pin_data jsonb)
returns void language plpgsql security definer set search_path = '' as $$
declare
  v_me uuid := (select auth.uid());
  v_username text;
  v_id bigint;
  v_visibility text;
  v_payload jsonb;
begin
  if v_me is null then raise exception 'Sign in first'; end if;
  if pin_data is null or jsonb_typeof(pin_data) <> 'object'
    or not (pin_data ? 'id') or pin_data ->> 'id' !~ '^-?[0-9]+$' then
    raise exception 'Invalid pin';
  end if;
  v_id := (pin_data ->> 'id')::bigint;
  select username into v_username from public.profiles where id = v_me;
  v_visibility := coalesce(pin_data ->> 'visibility',
    case when coalesce((pin_data ->> 'visible')::boolean, false)
      then 'public' else 'private' end);
  if v_visibility not in ('public', 'private', 'friends_only') then
    raise exception 'Invalid audience';
  end if;
  v_payload := jsonb_set(pin_data, '{owner}', to_jsonb(v_username));
  update public.pins set payload = v_payload, visibility = v_visibility
    where owner = v_me and pin_id = v_id;
  if not found then raise exception 'Pin does not exist'; end if;
end; $$;

create or replace function public.delete_my_pin(target_pin_id bigint)
returns void language plpgsql security definer set search_path = '' as $$
begin
  if (select auth.uid()) is null then raise exception 'Sign in first'; end if;
  delete from public.pins
    where owner = (select auth.uid()) and pin_id = target_pin_id;
  if not found then raise exception 'Pin does not exist'; end if;
end; $$;

revoke all on function public.save_my_pin(jsonb) from public, anon, authenticated;
revoke all on function public.edit_my_pin(jsonb) from public, anon, authenticated;
revoke all on function public.delete_my_pin(bigint) from public, anon, authenticated;
grant execute on function public.save_my_pin(jsonb) to authenticated;
grant execute on function public.edit_my_pin(jsonb) to authenticated;
grant execute on function public.delete_my_pin(bigint) to authenticated;

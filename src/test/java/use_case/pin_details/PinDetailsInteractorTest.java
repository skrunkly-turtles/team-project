package use_case.pin_details;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.select_location.SelectedLocation;
import use_case.pin_details.PinCreationDraft.Visibility;
import use_case.pin_details.PinDetailsOutputData.Stage;
import static org.junit.jupiter.api.Assertions.*;

/** Verifies draft lifecycle without Pin entities, storage, or UI dependencies. */
class PinDetailsInteractorTest {
    /** Opening requires a location, leaves optional fields empty, and selects no audience. */
    @Test
    void startsWithExplicitlyUnselectedAudience() {
        List<PinDetailsOutputData> results = new ArrayList<>();
        PinDetailsInteractor input = new PinDetailsInteractor(results::add);
        assertThrows(NullPointerException.class, () -> input.open(null));
        SelectedLocation location = new SelectedLocation(43, -79);
        input.open(location);
        PinCreationDraft draft = results.get(0).getDraft();
        assertSame(location, draft.getLocation());
        assertEquals("", draft.getThoughts());
        assertNull(draft.getImagePath());
        assertNull(draft.getVisibility());
        assertEquals(Stage.EDITING, results.get(0).getStage());
    }

    /** Back retains optional entries and audience while a revised location replaces the old one. */
    @Test
    void backRetainsValuesAndAllowsLocationRevision() {
        List<PinDetailsOutputData> results = new ArrayList<>();
        PinDetailsInteractor input = new PinDetailsInteractor(results::add);
        input.open(new SelectedLocation(1, 2));
        input.update("A thought", "/local/photo.png", Visibility.FRIENDS_ONLY);
        PinCreationDraft before = results.get(1).getDraft();
        input.back();
        assertEquals(Stage.BACK, results.get(2).getStage());
        assertSame(before, results.get(2).getDraft());
        input.update("ignored", null, Visibility.PUBLIC);
        assertEquals(3, results.size());
        SelectedLocation revised = new SelectedLocation(3, 4);
        input.open(revised);
        PinCreationDraft after = results.get(3).getDraft();
        assertSame(revised, after.getLocation());
        assertEquals("A thought", after.getThoughts());
        assertEquals("/local/photo.png", after.getImagePath());
        assertEquals(Visibility.FRIENDS_ONLY, after.getVisibility());
        assertNotSame(before, after);
    }

    /** Every audience can be selected; optional fields can be removed. */
    @Test
    void updatesAudienceAndOptionalFields() {
        List<PinDetailsOutputData> results = new ArrayList<>();
        PinDetailsInteractor input = new PinDetailsInteractor(results::add);
        input.open(new SelectedLocation(1, 2));
        for (Visibility visibility : Visibility.values()) {
            input.update(null, "  ", visibility);
            PinCreationDraft draft = results.get(results.size() - 1).getDraft();
            assertEquals(visibility, draft.getVisibility());
            assertEquals("", draft.getThoughts());
            assertNull(draft.getImagePath());
        }
    }

    /** Cancel discards values and subsequent requests cannot revive an old draft. */
    @Test
    void cancellationStartsNextPinFresh() {
        List<PinDetailsOutputData> results = new ArrayList<>();
        PinDetailsInteractor input = new PinDetailsInteractor(results::add);
        input.update("ignored", null, null);
        input.back();
        input.cancel();
        assertTrue(results.isEmpty());
        input.open(new SelectedLocation(1, 2));
        input.update("discard me", "photo.jpg", Visibility.PRIVATE);
        input.cancel();
        assertEquals(Stage.CANCELLED, results.get(2).getStage());
        assertNull(results.get(2).getDraft());
        input.cancel();
        input.update("ignored", null, null);
        assertEquals(3, results.size());
        input.open(new SelectedLocation(5, 6));
        assertEquals("", results.get(3).getDraft().getThoughts());
        assertNull(results.get(3).getDraft().getVisibility());
        assertNull(results.get(3).getDraft().getImagePath());
    }
    /** A completed library result survives details edits and Back, but never cancellation. */
    @Test
    void keepsSourceUntilDraftIsDiscarded() {
        List<PinDetailsOutputData> results = new ArrayList<>();
        PinDetailsInteractor input = new PinDetailsInteractor(results::add);
        SelectedPassage passage = new SelectedPassage(" Title ", null, " passage text ");
        assertEquals("Title", passage.getBookTitle());
        assertEquals("", passage.getAuthor());
        assertEquals("passage text", passage.getText());
        assertThrows(IllegalArgumentException.class, () -> new SelectedPassage("", "author", "text"));
        assertThrows(IllegalArgumentException.class, () -> new SelectedPassage("title", "author", " "));
        input.open(new SelectedLocation(1, 2), passage);
        input.update("thoughts", null, Visibility.PRIVATE);
        input.back();
        input.open(new SelectedLocation(3, 4));
        assertSame(passage, results.get(results.size() - 1).getDraft().getPassage());
        input.cancel();
        input.open(new SelectedLocation(5, 6));
        assertNull(results.get(results.size() - 1).getDraft().getPassage());
    }

}

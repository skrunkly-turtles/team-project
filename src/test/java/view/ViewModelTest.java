package view;

import interface_adapter.lookup_book.ReaderState;
import interface_adapter.lookup_book.LookupBookViewModel;
import models.ViewNames;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewModelTest {

    @Test
    void newViewModel_initialState() {
        LookupBookViewModel<String> viewModel = new LookupBookViewModel<>(ViewNames.LOOKUP_VIEW);

        assertEquals("lookup", viewModel.getState());
    }

    @Test
    void setState_updatesStateAndNotifiesListener() {
        LookupBookViewModel<ReaderState> viewModel = new LookupBookViewModel<>(new ReaderState());
        List<Object> newValuesSeen = new ArrayList<>();
        viewModel.addPropertyChangeListener(event -> newValuesSeen.add(event.getNewValue()));
        ReaderState pride = new ReaderState("Pride and Prejudice", "I must think your language too strong in speaking of both");

        viewModel.setState(pride);

        assertSame(pride, viewModel.getState());
        assertEquals(1, newValuesSeen.size());
        assertSame(pride, newValuesSeen.get(0));
    }

    @Test
    void setStateToSameValue_doesNotNotifyListener() {
        LookupBookViewModel<String> viewModel = new LookupBookViewModel<>(ViewNames.LOOKUP_VIEW);
        List<Object> newValuesSeen = new ArrayList<>();
        viewModel.addPropertyChangeListener(event -> newValuesSeen.add(event.getNewValue()));

        viewModel.setState(ViewNames.LOOKUP_VIEW);

        assertTrue(newValuesSeen.isEmpty());
    }
}

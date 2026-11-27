package interface_adapter.read_pins;

import interface_adapter.ViewModel;

/** Observable pins data read by the pins widget. */
public final class ReadPinsViewModel extends ViewModel<ReadPinsState> {
    /** Creates an empty pins ViewModel. */
    public ReadPinsViewModel() {
        super(new ReadPinsState());
    }
}
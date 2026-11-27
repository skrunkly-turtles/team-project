package use_case.read_pins;

/** Defines the action of loading the pins the signed-in user may see. */
public interface ReadPinsInputBoundary {
    /** Loads every pin the signed-in user has access to. */
    void execute();
}
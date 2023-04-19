module edict.edict_gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.json;
    requires org.apache.commons.lang3;
    requires com.opencsv;
    requires org.controlsfx.controls;
	requires com.fasterxml.jackson.databind;

    opens customControls;
    opens home to javafx.fxml;
    opens guimodel to javafx.base,com.fasterxml.jackson.databind;
    exports home;
}

module Run {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
	requires transitive javafx.graphics;
	
	requires java.persistence;
	requires transitive org.hibernate.orm.core;
	requires java.sql;
	requires java.desktop;
	requires java.xml;

    opens Run to javafx.fxml;
    exports Run;
    
    opens Controller to javafx.fxml;
    exports Controller;
    
    opens Entity to org.hibernate.orm.core, javafx.base;
    exports Entity;
}
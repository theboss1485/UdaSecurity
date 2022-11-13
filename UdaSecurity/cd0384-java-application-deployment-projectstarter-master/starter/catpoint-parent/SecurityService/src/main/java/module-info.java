module SecurityService {
    requires ImageService;
    requires java.desktop;
    requires miglayout.swing;
    requires java.prefs;
    requires com.google.gson;
    requires com.google.common;
    exports servicesecurity;
    opens data to com.google.gson;
}
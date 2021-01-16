package model;

public class Nature extends Special {
    private String buildmenu = "nature"; //optional

    public String getBuildmenu() {
        return buildmenu;
    }

    @Override
    public String toString() {
        return super.toString() + " Nature{" +
                "buildmenu='" + buildmenu + '\'' +
                '}';
    }
}

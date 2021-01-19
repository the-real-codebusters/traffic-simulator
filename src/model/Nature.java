package model;

public class Nature extends Special {

    @Override
    public Nature getNewInstance(){
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + " Nature{" +
                "buildmenu='" + buildmenu + '\'' +
                '}';
    }
}

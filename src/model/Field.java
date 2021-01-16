package model;

import java.util.Objects;

public class Field {

    private int height;
    private String fieldType;

    public Field(int height, String fieldType) {
        this.height = height;
        this.fieldType = fieldType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        if (height != field.height) return false;
        return Objects.equals(fieldType, field.fieldType);
    }

    @Override
    public int hashCode() {
        int result = height;
        result = 31 * result + (fieldType != null ? fieldType.hashCode() : 0);
        return result;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
}

package com.dahua.lib;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public abstract class MyStructure extends Structure {

    public MyStructure() {
    }

    public MyStructure(Pointer _pointer) {
        super(_pointer);
    }

    public static class ByReference extends MyStructure implements Structure.ByReference {
    }

    public static class ByValue extends MyStructure implements Structure.ByValue {
    }

    @Override
    protected List<String> getFieldOrder() {
        return getStructFields();
    }

    private List<String> getStructFields() {
        List<String> feilds = new LinkedList<>();
        Field[] declearedFeilds = this.getClass().getDeclaredFields();
        if (declearedFeilds != null && declearedFeilds.length > 0) {
            for (Field f : declearedFeilds) {
                feilds.add(f.getName());
            }
        }
        return feilds;
    }

    @Deprecated
    public void printStructFeilds() {
        List<String> feilds = getStructFields();
        if (feilds == null || feilds.size() == 0)
            System.out.println("feilds:none");
        else {
            StringBuilder feidlSb = new StringBuilder("feilds:");
            for (String f : feilds) {
                feidlSb.append(f);
                feidlSb.append(",");
            }
            System.out.println(feidlSb.toString());
        }
    }

}

package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * User: nbaker
 * Date: Sep 23, 2010
 */
public class JavascriptObjectCommand extends AbstractCommand{

    private JavaScriptObject func;

    public JavascriptObjectCommand(JavaScriptObject func){

        this.func = func;
    }

    @Override
    protected void performOperation() {
        performOperation(true);
    }

    @Override
    protected void performOperation(boolean feedback) {
        execFunc(func);
    }

    private native void execFunc(JavaScriptObject func)/*-{
        func();
    }-*/;
}

package org.pentaho.mantle.client.workspace;

public class JsBlock extends JsJobTrigger {
    // Overlay types always have protected, zero argument constructors.
    protected JsBlock() {
    }

    public final native int getBlockDuration() /*-{ return this.blockDuration; }-*/; //

}

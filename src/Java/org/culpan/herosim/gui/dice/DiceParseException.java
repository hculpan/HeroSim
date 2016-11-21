package org.culpan.herosim.gui.dice;

import org.culpan.herosim.gui.DamagePersonDialog;
import org.omg.PortableServer.THREAD_POLICY_ID;

/**
 * Created by USUCUHA on 11/21/2016.
 */
public class DiceParseException extends RuntimeException {
    public DiceParseException(String msg) {
        super(msg);
    }

    public DiceParseException(String msg, Throwable parent) {
        super(msg, parent);
    }
}

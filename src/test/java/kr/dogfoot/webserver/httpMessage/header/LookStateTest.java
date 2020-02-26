package kr.dogfoot.webserver.httpMessage.header;

import com.sun.org.apache.bcel.internal.generic.FADD;
import kr.dogfoot.webserver.server.resource.look.LookState;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

public class LookStateTest {
    @Test
    public void compareHeader() {
        LookState ls = new LookState("/a/b");

        assertEquals(ls.isLastPathItem(), false);
        assertEquals(ls.getNextPathItem(), "a");
        assertEquals(ls.isLastPathItem(), false);
        assertEquals(ls.getNextPathItem(), "b");
        /*
        assertEquals(ls.isLastPathItem(), false);
        assertEquals(ls.getNextPathItem(), "c.htm");
        assertEquals(ls.isLastPathItem(), true);
        */
    }

}

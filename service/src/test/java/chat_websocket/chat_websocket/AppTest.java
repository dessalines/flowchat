package chat_websocket.chat_websocket;

import org.javalite.activejdbc.LazyList;
import org.junit.*;
import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.*;
import com.chat.tools.Tools;
import static com.chat.db.Tables.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Before
    public void setUp() {
        Tools.dbInit();
    }

    @After
    public void tearDown() {
        Tools.dbClose();
    }


    @Test
    public void testJDBC() {
        LazyList<CommentTree> ct = COMMENT_TREE.where("parent_id = ?", 1);
        System.out.println(ct.toJson(true));

    }

    @Test
    public void testCommentObjJson() {
        

    }


}

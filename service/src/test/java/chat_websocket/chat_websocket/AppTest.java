package chat_websocket.chat_websocket;

import ch.qos.logback.classic.Logger;
import com.chat.db.Transformations;
import org.javalite.activejdbc.LazyList;
import org.junit.*;
import static org.junit.Assert.*;

import java.sql.Array;
import java.sql.SQLException;
import java.util.List;

import java.sql.Timestamp;
import java.util.*;
import com.chat.tools.Tools;
import org.postgresql.jdbc.PgArray;
import org.slf4j.LoggerFactory;

import static com.chat.db.Tables.*;
import static com.chat.db.Transformations.CommentObj;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static Logger log = (Logger) LoggerFactory.getLogger(AppTest.class);


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
        assertTrue(ct.get(0).getInteger("id").equals(1));

    }

    @Test
    public void testCommentObjJson() {
        LazyList<CommentThreadedView> ctv = COMMENT_THREADED_VIEW.where("discussion_id = ?", 1);


        List<CommentObj> cos = Transformations.convertCommentsToEmbeddedObjects(ctv);

        for (CommentObj co : cos) {
            System.out.println(co);
        }

        assertTrue(cos.get(0).getEmbedded().get(0).getId().equals(2));
    }

    @Test
    public void testCollect() throws SQLException {
        LazyList<CommentThreadedView> ctv = COMMENT_THREADED_VIEW.where("discussion_id = ?", 1);

        log.info(ctv.toJson(true));

        Long test = (Long) ctv.collect("id", "id", new Long(2)).get(0);

        assertTrue(test.equals(Long.valueOf(2)));

        Array parentBreadCrumbsStr = (Array) ctv.collect("breadcrumbs", "id", 4L).get(0);

        List<Long> list = Tools.convertArrayToList(parentBreadCrumbsStr);

        log.info(Arrays.toString(list.toArray()));

        assertTrue(list.get(0).equals(Long.valueOf(1)));


    }


}

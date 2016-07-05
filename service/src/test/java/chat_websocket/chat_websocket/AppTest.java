package chat_websocket.chat_websocket;

import ch.qos.logback.classic.Logger;
import com.chat.db.Transformations;
import com.chat.types.CommentObj;
import com.chat.types.Comments;
import com.chat.types.DiscussionObj;
import com.chat.webservice.Constants;
import org.javalite.activejdbc.LazyList;
import org.junit.*;
import static org.junit.Assert.*;

import java.sql.Array;
import java.sql.SQLException;
import java.util.List;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.IntStream;

import com.chat.tools.Tools;
import org.postgresql.jdbc.PgArray;
import org.slf4j.LoggerFactory;

import static com.chat.db.Tables.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static Logger log = (Logger) LoggerFactory.getLogger(AppTest.class);


    @Before
    public void setUp() {
        Constants.INSTANCE.getRankingConstants();
        Tools.dbInit();
    }

    @After
    public void tearDown() {
        Tools.dbClose();
    }


    @Test
    public void testJDBC() {
        LazyList<CommentTree> ct = CommentTree.where("parent_id = ?", 1);
        assertTrue(ct.get(0).getInteger("id").equals(1));

    }

    @Test
    public void testCommentObjJson() {
        LazyList<CommentThreadedView> ctv = CommentThreadedView.where("discussion_id = ?", 1);

        List<CommentObj> cos = Transformations.convertCommentsToEmbeddedObjects(ctv, null);

        assertTrue(cos.get(0).getEmbedded().get(0).getId().equals(2L));
    }

    @Test
    public void testCollect() throws SQLException {
        LazyList<CommentThreadedView> ctv = CommentThreadedView.where("discussion_id = ?", 1);

        log.info(ctv.toJson(true));

        Long test = (Long) ctv.collect("id", "id", new Long(2)).get(0);

        assertTrue(test.equals(Long.valueOf(2)));

        PgArray parentBreadCrumbsStr = (PgArray) ctv.collect("breadcrumbs", "id", 4L).get(0);

        List<Long> list = Tools.convertArrayToList(parentBreadCrumbsStr);

        log.info(Arrays.toString(list.toArray()));

        assertTrue(list.get(0).equals(Long.valueOf(1)));


    }

    @Test
    public void testFindComment() throws SQLException {
        LazyList<CommentThreadedView> ctv = CommentThreadedView.where("discussion_id = ?", 1);

        log.info(ctv.toJson(false));

        CommentThreadedView testFound = CommentThreadedView.findFirst("id = ?", 4L);

        log.info(testFound.toJson(false));

//        ctv.collect("", "id", 4L).get(0);

//        Integer index = ctv.indexOf(testFound);
//
//        log.info(index.toString());
//
//
//
        Integer index = Tools.findIndexByIdInLazyList(ctv, 4L);
        log.info(index.toString());
        log.info(ctv.get(index).toString());

//
//        log.info(ctv2.toString());

    }

    @Test
    public void testDiscussionConvert() throws SQLException {
        DiscussionFullView dfv = DiscussionFullView.findFirst("id = ?", 1);
        DiscussionObj df = DiscussionObj.create(dfv, null);

        assertTrue(df.getId().equals(1L));
    }

    @Test
    public void testDiscussionCollect() throws SQLException {
        LazyList<DiscussionNoTextView> dntvs = DiscussionNoTextView.findAll();
        Set<Long> ids = dntvs.collectDistinct("id");

        assertTrue(Tools.convertListToInQuery(ids).equals("(1, 2, 3)"));
    }

    @Test
    public void testNotification() throws SQLException {
        LazyList<CommentBreadcrumbsView> cbv = CommentBreadcrumbsView.where(
                "parent_user_id = ? and user_id != ? and read = false",
                4,4);

        Comments comments = Comments.replies(cbv);

        assertTrue(comments.getComments().get(0).getRead().equals(false));

    }


}

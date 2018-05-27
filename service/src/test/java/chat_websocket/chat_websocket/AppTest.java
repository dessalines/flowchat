package chat_websocket.chat_websocket;

import ch.qos.logback.classic.Logger;
import com.chat.webservice.ConstantsService;
import org.junit.*;
import static junit.framework.TestCase.assertFalse;

import com.chat.tools.Tools;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static Logger log = (Logger) LoggerFactory.getLogger(AppTest.class);

    @Before
    public void setUp() {
       ConstantsService.INSTANCE.getRankingConstants();
        Tools.dbInit();
    }

    @After
    public void tearDown() {
        Tools.dbClose();
    }


//    @Test
//    public void testJDBC() {
//        LazyList<CommentTree> ct = CommentTree.where("parent_id = ?", 1);
//        assertTrue(ct.get(0).getInteger("id").equals(1));
//
//    }
//
//    @Test
//    public void testCommentObjJson() {
//        LazyList<CommentThreadedView> ctv = CommentThreadedView.where("discussion_id = ?", 1);
//
//        List<Comment> cos = Transformations.convertCommentsToEmbeddedObjects(ctv, null);
//
//        assertTrue(cos.get(0).getEmbedded().get(0).getId().equals(2L));
//    }
//
//    @Test
//    public void testCollect() throws SQLException {
//        LazyList<CommentThreadedView> ctv = CommentThreadedView.where("discussion_id = ?", 1);
//
//        Long test = (Long) ctv.collect("id", "id", new Long(2)).get(0);
//
//        assertTrue(test.equals(Long.valueOf(2)));
//
//        PgArray parentBreadCrumbsStr = (PgArray) ctv.collect("breadcrumbs", "id", 4L).get(0);
//
//        List<Long> list = Tools.convertArrayToList(parentBreadCrumbsStr);
//
//        assertTrue(list.get(0).equals(Long.valueOf(1)));
//
//    }
//
//    @Test
//    public void testFindComment() throws SQLException {
//        LazyList<CommentThreadedView> ctv = CommentThreadedView.where("discussion_id = ?", 1);
//
//        CommentThreadedView testFound = CommentThreadedView.findFirst("id = ?", 4L);
//
//        Integer index = Tools.findIndexByIdInLazyList(ctv, 4L);
//        assertTrue(index.equals(2));
//
//    }
//
//    @Test
//    public void testDiscussionConvert() throws SQLException {
//        DiscussionFullView dfv = DiscussionFullView.findFirst("id = ?", 1);
//        Discussion df = com.chat.types.discussion.Discussion.create(dfv, null, null, null, null, null);
//
//        assertTrue(df.getId().equals(1L));
//    }
//
//    @Test
//    public void testDiscussionCollect() throws SQLException {
//        LazyList<DiscussionNoTextView> dntvs = DiscussionNoTextView.findAll();
//        Set<Long> ids = dntvs.collectDistinct("id");
//
//        assertTrue(Tools.convertListToInQuery(ids).contains("(1, 2, 3"));
//    }
//
//    @Test
//    public void testNotification() throws SQLException {
//        LazyList<CommentBreadcrumbsView> cbv = CommentBreadcrumbsView.where(
//                "parent_user_id = ? and user_id != ? and read = false",
//                4,4);
//
//        Comments comments = Comments.replies(cbv);
//
////        assertTrue(comments.getComments().get(0).getRead().equals(false));
//
//    }
//
    @Test
    public void testEncrypt() {
        String encryptedPassword = Tools.PASS_ENCRYPT.encryptPassword("asdf");
        log.info(encryptedPassword);
    }

}

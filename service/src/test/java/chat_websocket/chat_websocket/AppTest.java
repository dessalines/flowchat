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
    @Test
    public void testJDBC() {
        Tools.dbInit();
        LazyList<CommentTree> ct = COMMENT_TREE.where("parent_id = ?", 1);

        System.out.println(ct.toJson(true));

        
    }

    public static class CommentObj {
        private Integer id, topParentId, parentId, childId, pathLength;
        private Timestamp created;
        private List<CommentObj> embedded;
        private List<Integer> breadcrumbs;

        public CommentObj(Integer id,
                          Integer topParentId,
                          Integer childId,
                          Integer pathLength,
                          Timestamp created,
                          String breadcrumbs) {
            this.id = id;
            this.topParentId = topParentId;
            this.childId = childId;
            this.pathLength = pathLength;
            this.created = created;
            this.embedded = new ArrayList<>();

            setBreadCrumbsArr(breadcrumbs);
            setParentId();

        }

        private void setBreadCrumbsArr(String breadCrumbs) {
            breadcrumbs = new ArrayList<>();
            for (String br : breadCrumbs.split(",")) {
                breadcrumbs.add(Integer.valueOf(br));
            }
        }

        private void setParentId() {
            Integer cIndex = breadcrumbs.indexOf(id);

            if (cIndex > 0) {
                parentId = breadcrumbs.get(cIndex - 1);
            }

//            topParentId = breadcrumbs.get(0);

        }
    }
}

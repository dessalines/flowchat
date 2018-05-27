package com.chat.scheduled;


import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Actions;
import com.chat.db.Tables;
import com.chat.tools.Tools;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;

public class RedditImporter implements Job {

    public static Logger log = (Logger) LoggerFactory.getLogger(RedditImporter.class);

    private RedditClient redditClient;

    public void init() {

        try {
            UserAgent myUserAgent = UserAgent.of("desktop", "com.flowchat", "v0.1", "dessalines");
            redditClient = new RedditClient(myUserAgent);
            Credentials credentials = Credentials.script(DataSources.PROPERTIES.getProperty("reddit_username"),
                    DataSources.PROPERTIES.getProperty("reddit_password"),
                    DataSources.PROPERTIES.getProperty("reddit_client_id"),
                    DataSources.PROPERTIES.getProperty("reddit_client_secret"));
            OAuthData authData = redditClient.getOAuthHelper().easyAuth(credentials);
            redditClient.authenticate(authData);
        } catch (OAuthException e) {
            e.printStackTrace();
        }
    }

    public void fetchTopPosts() {

        init();

        log.info("Fetching top reddit posts...");

        SubredditPaginator paginator = new SubredditPaginator(redditClient);
        paginator.setSubreddit("popular");
        paginator.setTimePeriod(TimePeriod.DAY);
        paginator.setSorting(Sorting.TOP);
        paginator.setLimit(50);
        Integer pageLimit = 100;

        Tools.dbInit();
        for (int i = 0; i < pageLimit; i++) {
            Listing<Submission> currentPage = paginator.next();
            for (Submission s : currentPage) {
                Tables.Tag t = Actions.getOrCreateTagFromSubreddit(s.getSubredditName());

                Actions.getOrCreateDiscussionFromRedditPost(t.getLongId(),
                        StringUtils.abbreviate(s.getTitle().replaceAll("\\r|\\n", "").replaceAll("\"", "").trim(), 140),
                        s.getUrl(),
                        s.getSelftext(),
                        s.getCreated());

            }
        }
        Tools.dbClose();

        log.info("Done fetching top reddit posts.");
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        fetchTopPosts();
    }

}

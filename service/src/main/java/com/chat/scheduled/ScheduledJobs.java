package com.chat.scheduled;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;


/**
 * Created by tyler on 5/30/17.
 */

public class ScheduledJobs {
    public static void start() {
        // Another
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            scheduler.start();

            JobDetail fetchFromReddit = newJob(RedditImporter.class)
                    .build();

            // Trigger the job to run now, and then repeat every x minutes
            Trigger fetchFromRedditTrigger = newTrigger()
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInHours(4)
                            .repeatForever())
                    .build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(fetchFromReddit, fetchFromRedditTrigger);

        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }
}

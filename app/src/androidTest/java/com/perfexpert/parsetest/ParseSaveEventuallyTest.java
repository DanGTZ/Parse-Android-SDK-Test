package com.perfexpert.parsetest;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseSession;
import com.parse.ParseUser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Claude Joseph-Angélique on 17/05/18.
 * Class to show saveEventually issue in Parse-SDK-Android
 */
@RunWith(AndroidJUnit4.class)
public class ParseSaveEventuallyTest {

    private ParseUser mUser;
    private ParseSession mSession;
    private ParseException mException;

    final static private String USERNAME = "ParseSaveEventuallyTest";
    final static private String PASSWORD = "ParseSaveEventuallyTest";
    final static private String EMAIL = "parsesaveeventuallytest@test.com";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {
        final String tag = "setUp";

        Log.d(tag, "Logging out...");
        ParseUser.logOut();

        try {
            Log.d(tag, "Logging in...");
            mUser = ParseUser.logIn(USERNAME, PASSWORD);
        } catch (ParseException e) {
            Log.d(tag, "User doesn't exists, creating it...");
            Assert.assertEquals(e.getCode(), ParseException.OBJECT_NOT_FOUND);
            mUser = new ParseUser();
            mUser.setEmail(EMAIL);
            mUser.setUsername(USERNAME);
            mUser.setPassword(PASSWORD);
            mUser.signUp();
        }
        Assert.assertEquals(mUser, ParseUser.getCurrentUser());
        Assert.assertEquals(mUser.getUsername(), USERNAME);
        Assert.assertEquals(mUser.getEmail(), EMAIL);

        Log.d(tag, "Checking session...");
        final CountDownLatch lockSession = new CountDownLatch(1);
        ParseSession.getCurrentSessionInBackground().continueWith(new Continuation<ParseSession, Void>() {
            @Override
            public Void then(Task<ParseSession> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(tag, task.getError().getLocalizedMessage());
                }
                mSession = task.getResult();
                lockSession.countDown();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
        Assert.assertTrue(lockSession.await(10, TimeUnit.SECONDS));
        Assert.assertNotNull(mSession);
    }

    @Test
    public void testSaveEventuallyInstallation() throws Exception {

        final String tag = "testSaveEventuallyInstallation";

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        Log.d(tag, "Saving installation...");
        installation.put("field", "data");
        installation.save();

        Log.d(tag, "Disconnecting user...");
        ParseCloud.callFunction("closeAllSessions", new HashMap<String, Object>());

        Log.d(tag, "Checking session...");
        final CountDownLatch lockGetSession = new CountDownLatch(1);
        ParseSession.getCurrentSessionInBackground().continueWith(new Continuation<ParseSession, Void>() {
            @Override
            public Void then(Task<ParseSession> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(tag, task.getError().getLocalizedMessage());
                    mException = (ParseException)task.getError();
                }
                mSession = task.getResult();
                lockGetSession.countDown();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
        Assert.assertTrue(lockGetSession.await(10, TimeUnit.SECONDS));
        Assert.assertNull(mSession);
        Assert.assertEquals(mException.getCode(), ParseException.INVALID_SESSION_TOKEN);

        Log.d(tag, "Saving installation eventually...");
        installation.put("field", "other data");
        final CountDownLatch lockSaveEventually = new CountDownLatch(1);
        installation.saveEventually().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(tag, task.getError().getLocalizedMessage());
                    mException = (ParseException)task.getError();
                }
                lockSaveEventually.countDown();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
        Assert.assertTrue(lockSaveEventually.await(10, TimeUnit.SECONDS));
        Assert.assertEquals(mException.getCode(), ParseException.INVALID_SESSION_TOKEN);

        Log.d(tag, "Saving installation again...");
        installation.put("field", "another data");
        final CountDownLatch lockSaveInbackground = new CountDownLatch(1);
        installation.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(tag, task.getError().getLocalizedMessage());
                    mException = (ParseException)task.getError();
                }
                lockSaveInbackground.countDown();
                return null;
            }
        });
        Assert.assertTrue(lockSaveInbackground.await(10, TimeUnit.SECONDS));
        Assert.assertEquals(mException.getCode(), ParseException.INVALID_SESSION_TOKEN);
    }

    @Test
    public void testSaveEventuallyUser() throws Exception {

        final String tag = "testSaveEventuallyUser";

        Log.d(tag, "Saving user...");
        mUser.put("field", "data");
        mUser.save();

        Log.d(tag, "Disconnecting user...");
        ParseCloud.callFunction("closeAllSessions", new HashMap<String, Object>());

        Log.d(tag, "Checking session...");
        final CountDownLatch lockGetSession = new CountDownLatch(1);
        ParseSession.getCurrentSessionInBackground().continueWith(new Continuation<ParseSession, Void>() {
            @Override
            public Void then(Task<ParseSession> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(tag, task.getError().getLocalizedMessage());
                    mException = (ParseException)task.getError();
                }
                mSession = task.getResult();
                lockGetSession.countDown();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
        Assert.assertTrue(lockGetSession.await(10, TimeUnit.SECONDS));
        Assert.assertNull(mSession);
        Assert.assertEquals(mException.getCode(), ParseException.INVALID_SESSION_TOKEN);

        Log.d(tag, "Saving user eventually...");
        mUser.put("field", "other data");
        final CountDownLatch lockSaveEventually = new CountDownLatch(1);
        mUser.saveEventually().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(tag, task.getError().getLocalizedMessage());
                }
                lockSaveEventually.countDown();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
        Assert.assertTrue(lockSaveEventually.await(10, TimeUnit.SECONDS));
        Assert.assertEquals(mException.getCode(), ParseException.INVALID_SESSION_TOKEN);

        Log.d(tag, "Saving user again...");
        mUser.put("field", "another data");
        final CountDownLatch lockSaveInbackground = new CountDownLatch(1);
        mUser.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(tag, task.getError().getLocalizedMessage());
                    mException = (ParseException)task.getError();
                }
                lockSaveInbackground.countDown();
                return null;
            }
        });
        Assert.assertTrue(lockSaveInbackground.await(10, TimeUnit.SECONDS));
        Assert.assertEquals(mException.getCode(), ParseException.INVALID_SESSION_TOKEN);
    }
}

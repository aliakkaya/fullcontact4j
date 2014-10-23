package com.fullcontact.api.libs.fullcontact4j.request;

import com.fullcontact.api.libs.fullcontact4j.FullContactApi;
import com.fullcontact.api.libs.fullcontact4j.Utils;
import com.fullcontact.api.libs.fullcontact4j.enums.RateLimiterPolicy;
import com.fullcontact.api.libs.fullcontact4j.guava.RateLimiter;
import com.fullcontact.api.libs.fullcontact4j.response.FCResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class handles requests made by the client.
 * When a request is made, it is sent to an ExecutorService which
 * accounts for rate limiting and then sends the request.
 */
public class RequestExecutorHandler {

    //will execute the requests on a separate thread.
    protected final ExecutorService executorService;

    //if not null, will limit the request rate.
    private RateLimiter rateLimiter;
    private final RateLimiterPolicy policy;

    public RequestExecutorHandler(RateLimiterPolicy policy, Integer threadPoolCount) {
        this.policy = policy;
        executorService = Executors.newFixedThreadPool(threadPoolCount);
    }

    /**
     * Sets the maximum requests per second. Requests are unlimited until the first response,
     * when the maximum amount is set from the rate limiting headers. It cannot be changed after that.
     * @param requestsPerMinute
     */
    public void setRateLimitPerMinute(Integer requestsPerMinute) {
        if(rateLimiter == null) {
            Integer requestsPerSecond = requestsPerMinute / 60;

            if(policy == RateLimiterPolicy.BURST) {
                rateLimiter = RateLimiter.create(RateLimiter.SleepingStopwatch.createFromSystemTimer(),
                        requestsPerSecond, 5.0);
            } else {
                rateLimiter = RateLimiter.create(requestsPerSecond);
            }
        }
    }

    public <T extends FCResponse> void sendRequestAsync(final FullContactApi api, final FCRequest<T> req,
                                                        final FCCallback<T> callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                waitForPermit();
                Utils.verbose("Sending a new asynchronous " + req.getClass().getSimpleName());
                req.makeRequest(api, callback.getCoreCallback());
            }
        });
    }

    /**
     * Waits until the rate limiter will permit the request, or under RateLimiterPolicy.REJECT,
     * will throw an exception if a request can't be made.
     */
    protected void waitForPermit() {
        if(rateLimiter != null) {
            Utils.verbose("Waiting for ratelimiter to allow a request...");
            rateLimiter.acquire();
        }
    }

}
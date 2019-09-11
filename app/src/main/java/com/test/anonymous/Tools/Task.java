package com.test.anonymous.Tools;

import java.util.Timer;
import java.util.TimerTask;

public class Task {

    private Timer timer;
    private TimerTask timerTask;

    public Task(Timer timer, TimerTask timerTask) {
        this.timer = timer;
        this.timerTask = timerTask;
    }

    public void activateTask(long delay , long period){
        this.timer.schedule(this.timerTask , delay , period);
    }

    public void disableTask(){
        if(timer != null){
            this.timer.cancel();
            this.timer = null;
            this.timerTask.cancel();
            this.timerTask = null;
        }
    }
}

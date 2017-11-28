import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nathanielwendt on 4/2/17.
 */
public class Program3 {

    private class Activity{
        String name;
        int funLevel;
        int riskLevel;

        public Activity(String name, int funLevel, int riskLevel) {
            this.name = name;
            this.funLevel = funLevel;
            this.riskLevel = riskLevel;
        }

        public String getName() {
            return name;
        }

        public int getFunLevel() {
            return funLevel;
        }

        public int getRiskLevel() {
            return riskLevel;
        }
    }

    public ActivityResult selectActivity(ActivityProblem activityProblem){
        Activity[] activities= new Activity[activityProblem.getActivities().length];
        int[][] memo = new int[activities.length][activityProblem.getRiskBudget()+1];

        //initialize memo: -1 meaning not visited
        for(int i = 0; i < memo.length; i++){
            for(int j = 1; j < memo[0].length; j++){
                memo[i][j] = -1;
            }
        }

        for(int i = 0; i < memo.length; i++){
            activities[i] = new Activity(activityProblem.getActivities()[i], activityProblem.getFunLevels()[i],activityProblem.getRiskLevels()[i]);
        }

        int maxFunLvl = rec(activities, memo, memo.length-1, activityProblem.getRiskBudget());
        Set<String> selectedActivities = trace(activities, memo, memo.length-1, memo[0].length-1);

        ActivityResult ar = new ActivityResult();
        ar.setMaxFunLevel(maxFunLvl);
        ar.setSelectedActivities(selectedActivities);
        return ar;
    }

    private int rec(Activity[] activities, int[][] memo, int act, int rem_budget){
        //boundary conditions
        if(act == -1 || rem_budget == 0){
            return 0;
        }
        //if current cell has not yet been visited
        else if(memo[act][rem_budget] != -1) {
            return memo[act][rem_budget];
        }
        //if we don't have enough budget for the current activity, then only one choice: skip it
        else if(rem_budget < activities[act].getRiskLevel()){
            memo[act][rem_budget] = rec(activities, memo, act - 1, rem_budget);
            return memo[act][rem_budget];
        }
        //if we do have enough budget for the current activity, then we can compare the optimal values for taking and not taking the current activity
        else{
            int notTake = rec(activities, memo, act - 1, rem_budget);
            int take = rec(activities, memo, act-1, rem_budget-activities[act].getRiskLevel())+activities[act].getFunLevel();
            //choose and record the optimal value for the current cell
            memo[act][rem_budget] = Math.max(notTake, take);
            return memo[act][rem_budget];
        }
    }

    public Set<String> trace(Activity[] activities, int[][] memo, int row, int col){
        Set<String> s = new HashSet<>();

        //boundary conditions
        if(col == 0){
            return s;
        }
        else if(row == 0){//if we are on the first row, we still need to determine whether the first activity was taken or not
            if(memo[0][col] != 0){
                s.add(activities[row].getName());
            }
            return s;
        }

        //see if the current activity was skipped (true if the value directly above the current cell is the same as the value in the current cell)
        else if(memo[row][col] == memo[row-1][col]){
            return(trace(activities, memo, row-1, col));
        }
        //current activity was taken, go back up one level and reduce the column number
        else{
            s.add(activities[row].getName());
            s.addAll(trace(activities, memo, row-1, col - activities[row].getRiskLevel()));
        }
        return s;
    }

    public SchedulingResult selectScheduling(SchedulingProblem schedulingProblem){
        int[] mauiCosts = schedulingProblem.getMauiCosts();
        int[] oahuCosts = schedulingProblem.getOahuCosts();
        int transferCost = schedulingProblem.getTransferCost();

        int[][]choices = new int[mauiCosts.length][2];//on each row, column 0 represents the opt value for chosing maui, column 1 represents the opt value for choosing oahu.
        choices[0][0] = mauiCosts[0];
        choices[0][1] = oahuCosts[0];

        for(int i = 1; i < choices.length; i++){//algorithm described in report
            choices[i][0] = Math.min(choices[i-1][0] + mauiCosts[i], choices[i-1][1] + transferCost + mauiCosts[i]);
            choices[i][1] = Math.min(choices[i-1][0] + transferCost + oahuCosts[i], choices[i-1][1] + oahuCosts[i]);
        }


        boolean[] schedule = new boolean[mauiCosts.length];

        //check the last location we ended up at.
        String currentLoc;
        if(choices[choices.length-1][0]<choices[choices.length-1][1]){
            currentLoc = "Maui";
        }
        else{
            currentLoc = "Oahu";
        }

        //trace back solution
        scheduleTrace(choices, schedule, choices.length-1,currentLoc,mauiCosts,oahuCosts, transferCost);

        SchedulingResult sr = new SchedulingResult();
        sr.setSchedule(schedule);
        return sr;
    }

    private void scheduleTrace(int[][] choices, boolean[]schedule, int idx, String currentLoc, int[] mauiCosts, int[] oahuCosts, int transferCost){
        schedule[idx] = currentLoc.equals("Maui") ? true : false;
        if(idx == 0){
            return;
        }
        //if we are currently at Maui
        if(currentLoc.equals("Maui")){
            //if the previous location is from maui
            if(choices[idx-1][0]+ mauiCosts[idx] == choices[idx][0]){
                scheduleTrace(choices,schedule,idx-1,"Maui",mauiCosts, oahuCosts, transferCost);
            }
            //if the previous location is from oahu
            else{
                scheduleTrace(choices,schedule,idx-1,"Oahu",mauiCosts, oahuCosts, transferCost);
            }
        }
        //if we are currently at Oahu
        else{
            //if the previous location is from maui
            if(choices[idx-1][0] +  transferCost + oahuCosts[idx] == choices[idx][1]){
                scheduleTrace(choices,schedule,idx-1,"Maui",mauiCosts, oahuCosts, transferCost);
            }
            //if the previous location is from oahu
            else{
                scheduleTrace(choices,schedule,idx-1,"Oahu",mauiCosts, oahuCosts, transferCost);
            }
        }
    }
}
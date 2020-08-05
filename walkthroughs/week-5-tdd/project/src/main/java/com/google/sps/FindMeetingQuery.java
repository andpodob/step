// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Stack;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    /**
     * importantRanges - store time ranges that are important for at least one attendee in the request
     * busyIntervals - eventually store merged important time ranges
     * freeIntervals - used to collect intevals that are between busyIntervals   
     */
    ArrayList<TimeRange> importantRanges = new ArrayList<TimeRange>();
    Stack<TimeRange> busyIntervals = new Stack<TimeRange>();
    ArrayList<TimeRange> freeIntervals = new ArrayList<TimeRange>();
    
    for(String attendee : request.getAttendees()){
      for (Event event : events){
        if(event.getAttendees().contains(attendee)){
          importantRanges.add(event.getWhen());
        }
      }
    }

    importantRanges.sort(TimeRange.ORDER_BY_START);
   
    TimeRange popedRange;
    for(TimeRange timeRange : importantRanges){
      if(busyIntervals.size() > 0 && timeRange.overlaps(busyIntervals.peek())){
        popedRange = busyIntervals.pop();
        busyIntervals.push(mergeTimeRanges(popedRange, timeRange));
      }else{
        busyIntervals.push(timeRange);
      }
    }

    int duration;
    //dummy TimeRanges to provide limits to the entire space
    busyIntervals.add(0, new TimeRange(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY));
    busyIntervals.add(new TimeRange(TimeRange.END_OF_DAY+1, TimeRange.END_OF_DAY+1));
    //this loop calculates size of free space between two subsequent intervals, and checks if size meets requirements
    //if so it adds this space to freeIntervals that stores possible place for requested event
    for(int i = 0; i < busyIntervals.size()-1; i++){
      duration = busyIntervals.get(i+1).start()-busyIntervals.get(i).end();
      if(duration > 0 && duration >= request.getDuration())
        freeIntervals.add(new TimeRange(busyIntervals.get(i).end(), busyIntervals.get(i+1).start()-busyIntervals.get(i).end()));
    }

    return freeIntervals;
  }

  private TimeRange mergeTimeRanges(TimeRange a, TimeRange b){
    if(a.overlaps(b)){
      int begin = Math.min(a.start(), b.start()), end = Math.max(a.end(),b.end());
      return new TimeRange(begin, end-begin);
    }else{
      return null;
    }
  } 
}

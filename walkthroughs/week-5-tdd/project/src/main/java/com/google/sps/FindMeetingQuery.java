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
     * requiredBusyRanges - store time ranges that are important for at least one mandatory attendee in the request (doesn't apply to optional attendees)
     * allBusyRanges - store time ranges that are important for mandatory as well as optional attendees 
     * requiredBusyRangesMerged - eventually store merged important time ranges
     * allBusyRangesMerged - same as above but for mandatory and optional attendees 
     */
    ArrayList<TimeRange> requiredBusyRanges = new ArrayList<TimeRange>();
    ArrayList<TimeRange> allBusyRanges = new ArrayList<TimeRange>();
    Stack<TimeRange> requiredBusyRangesMerged = new Stack<TimeRange>();
    Stack<TimeRange> allBusyRangesMerged = new Stack<TimeRange>();

    for (Event event : events){
      for (String attendee : request.getAttendees()){
        if(event.getAttendees().contains(attendee)){
          requiredBusyRanges.add(event.getWhen());
          allBusyRanges.add(event.getWhen());
          break;
        }
      }
    }

    for (Event event : events){
      for (String attendee : request.getOptionalAttendees()){
        if(event.getAttendees().contains(attendee)){
          allBusyRanges.add(event.getWhen());
          break;
        }
      }
    }

    // after two above steps in allBusyRanges we have important ranges for optional and mandatory attendees and in
    // requiredBusyRanges ranges important for only mandatory attendees

    //sorting ranges by start to ease process of merging intervals
    allBusyRanges.sort(TimeRange.ORDER_BY_START);
    requiredBusyRanges.sort(TimeRange.ORDER_BY_START);
    
    //merging intervals in both requiredBusyRanges and allBusyRanges
    TimeRange topRange;
    if(requiredBusyRanges.size() > 0) requiredBusyRangesMerged.push(requiredBusyRanges.get(0));
    
    for(TimeRange timeRange : requiredBusyRanges){
      if(timeRange.overlaps(requiredBusyRangesMerged.peek())){
        topRange = requiredBusyRangesMerged.pop();
        requiredBusyRangesMerged.push(mergeTimeRanges(topRange, timeRange));
      }else{
        requiredBusyRangesMerged.push(timeRange);
      }
    }
    
  
    if(allBusyRanges.size() > 0) allBusyRangesMerged.push(allBusyRanges.get(0));
    for(TimeRange timeRange : allBusyRanges){
      if(timeRange.overlaps(allBusyRangesMerged.peek())){
        topRange = allBusyRangesMerged.pop();
        allBusyRangesMerged.push(mergeTimeRanges(topRange, timeRange));
      }else{
        allBusyRangesMerged.push(timeRange);
      }
    }

    int duration;
    //dummy TimeRanges to provide limits to the entire space
    requiredBusyRangesMerged.add(new TimeRange(TimeRange.END_OF_DAY+1, TimeRange.END_OF_DAY+1));
    requiredBusyRangesMerged.add(0, new TimeRange(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY));
    allBusyRangesMerged.add(new TimeRange(TimeRange.END_OF_DAY+1, TimeRange.END_OF_DAY+1));
    allBusyRangesMerged.add(0, new TimeRange(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY));

    //this loop calculates size of free space between two subsequent intervals, and checks if size meets requirements
    //if so it adds this space to freeIntervals that stores possible place for requested event
    ArrayList<TimeRange> freeRanges = new ArrayList<TimeRange>();
    for(int i = 0; i < allBusyRangesMerged.size()-1; i++){
      duration = allBusyRangesMerged.get(i+1).start()-allBusyRangesMerged.get(i).end();
      if(duration > 0 && duration >= request.getDuration())
        freeRanges.add(new TimeRange(allBusyRangesMerged.get(i).end(), allBusyRangesMerged.get(i+1).start()-allBusyRangesMerged.get(i).end()));
    }

    //if we haven't found any free space with optional attendees we calculate free space ranges without optional
    if(freeRanges.size() == 0){
      for(int i = 0; i < requiredBusyRangesMerged.size()-1; i++){
        duration = requiredBusyRangesMerged.get(i+1).start()-requiredBusyRangesMerged.get(i).end();
        if(duration > 0 && duration >= request.getDuration())
          freeRanges.add(new TimeRange(requiredBusyRangesMerged.get(i).end(), requiredBusyRangesMerged.get(i+1).start()-requiredBusyRangesMerged.get(i).end()));
      }
    }
    return freeRanges;
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

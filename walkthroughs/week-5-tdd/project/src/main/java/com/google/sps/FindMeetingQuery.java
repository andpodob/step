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
import java.util.Arrays;
import java.util.Stack;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    /**
     * importantRanges - store time ranges that are important for at leas on attendee in the request
     * busyIntervals - eventualy store merged important time ranges
     * freeIntervals - used to collect intevals that are between busyIntervals   
     */
    ArrayList<TimeRange> importantRanges = new ArrayList<TimeRange>();
    ArrayList<TimeRange> optionalImportantRanges = new ArrayList<TimeRange>();
    Stack<TimeRange> busyIntervals = new Stack<TimeRange>();
    Stack<TimeRange> optionalBusyIntervals = new Stack<TimeRange>();
    ArrayList<TimeRange> freeIntervals = new ArrayList<TimeRange>();
    ArrayList<TimeRange> freeIntervalsWithOptionals = new ArrayList<TimeRange>();
    
    for(String attendee : request.getAttendees()){
      for (Event event : events){
        if(event.getAttendees().contains(attendee)){
          importantRanges.add(event.getWhen());
          optionalImportantRanges.add(event.getWhen());
        }
      }
    }
    
    for(String attendee : request.getOptionalAttendees()){
      for (Event event : events){
        if(event.getAttendees().contains(attendee)){
          optionalImportantRanges.add(event.getWhen());
        }
      }
    }
    //if there are no occupied or optionaly occupied intervals to consider we can immediately return list with WHOLE_DAY
    if(optionalImportantRanges.size() == 0){
      if(request.getDuration() <= TimeRange.WHOLE_DAY.duration()){
        return Arrays.asList(TimeRange.WHOLE_DAY);
      } else{
        return Arrays.asList();
      }
    }

    optionalImportantRanges.sort(TimeRange.ORDER_BY_START);
    importantRanges.sort(TimeRange.ORDER_BY_START);
    
    TimeRange popedRange;
    if(importantRanges.size() > 0){
      busyIntervals.push(importantRanges.get(0));
      for(TimeRange timeRange : importantRanges){
        if(timeRange.overlaps(busyIntervals.peek())){
          popedRange = busyIntervals.pop();
          busyIntervals.push(mergeTimeRanges(popedRange, timeRange));
        }else{
          busyIntervals.push(timeRange);
        }
      }
    }
  
    optionalBusyIntervals.push(optionalImportantRanges.get(0));
    for(TimeRange timeRange : optionalImportantRanges){
      if(timeRange.overlaps(optionalBusyIntervals.peek())){
        popedRange = optionalBusyIntervals.pop();
        optionalBusyIntervals.push(mergeTimeRanges(popedRange, timeRange));
      }else{
        optionalBusyIntervals.push(timeRange);
      }
    }
    int duration;
    //dummy TimeRanges to provide limits to the entire space
    busyIntervals.add(0, new TimeRange(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY));
    busyIntervals.add(new TimeRange(TimeRange.END_OF_DAY+1, TimeRange.END_OF_DAY+1));
    optionalBusyIntervals.add(0, new TimeRange(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY));
    optionalBusyIntervals.add(new TimeRange(TimeRange.END_OF_DAY+1, TimeRange.END_OF_DAY+1));
    //this loop calculates size of free space between two subsequent intervals, and checks if size meets requirements
    //if so it adds this space to freeIntervals that stores possible place for requested event
    for(int i = 0; i < busyIntervals.size()-1; i++){
      duration = busyIntervals.get(i+1).start()-busyIntervals.get(i).end();
      if(duration > 0 && duration >= request.getDuration())
        freeIntervals.add(new TimeRange(busyIntervals.get(i).end(), busyIntervals.get(i+1).start()-busyIntervals.get(i).end()));
    }
    for(int i = 0; i < optionalBusyIntervals.size()-1; i++){
      duration = optionalBusyIntervals.get(i+1).start()-optionalBusyIntervals.get(i).end();
      if(duration > 0 && duration >= request.getDuration())
        freeIntervalsWithOptionals.add(new TimeRange(optionalBusyIntervals.get(i).end(), optionalBusyIntervals.get(i+1).start()-optionalBusyIntervals.get(i).end()));
    }

    //
    if(freeIntervalsWithOptionals.size() == 0) return freeIntervals;
    else return freeIntervalsWithOptionals;
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

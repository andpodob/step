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
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    /**
     * importantRanges - store time ranges that are important for at least one mandatory attendee in the request (doesn't apply to opitonal attendees)
     * optionalImportantRanges - store time ranges that are important for mandatory as well as optional attendees 
     * busyIntervals - eventualy store merged important time ranges
     * optionalBusyRanges - same as above but for mandatory and optional attendees
     * freeIntervals - used to collect intevals that are between busyIntervals
     * freeRangesWithOptionals - same as above but for mandatory and optional attendees   
     */
    ArrayList<TimeRange> importantRanges = new ArrayList<TimeRange>();
    ArrayList<TimeRange> optionalImportantRanges = new ArrayList<TimeRange>();
    Stack<TimeRange> busyRanges = new Stack<TimeRange>();
    Stack<TimeRange> optionalBusyRanges = new Stack<TimeRange>();
    ArrayList<TimeRange> freeRanges = new ArrayList<TimeRange>();
    ArrayList<TimeRange> freeRangesWithOptionals = new ArrayList<TimeRange>();
    
    //list that helps in optimizing search for important events
    LinkedList<Event> eventsList = new LinkedList<Event>(events);
    LinkedList<Event> eventsLeft = eventsList;
    //populates importantRanges and optionalImportantRanges with ranges that are important for mandatory attendees
    for(String attendee : request.getAttendees()){
      eventsList = eventsLeft;
      eventsLeft = new LinkedList<Event>();
      for (Event event : eventsList){
        if(event.getAttendees().contains(attendee)){
          importantRanges.add(event.getWhen());
          optionalImportantRanges.add(event.getWhen());
        } else{
          eventsLeft.add(event);
        }
      }
    }
    //same as above but for optional attendees
    eventsList = new LinkedList<Event>(events);
    eventsLeft = eventsList;
    for(String attendee : request.getOptionalAttendees()){
      eventsList = eventsLeft;
      eventsLeft = new LinkedList<Event>();
      for (Event event : eventsList){
        if(event.getAttendees().contains(attendee)){
          optionalImportantRanges.add(event.getWhen());
        } else{
          eventsLeft.add(event);
        }
      }
    }
    // after two above steps in optionalImportantRanges we have important ranges for optional and mandatory attendees and in
    // importantRanges ranges important for only mandatory attendees

    // if there are no occupied or optionaly occupied intervals to consider we can immediately return list with WHOLE_DAY
    // or empty list if WHOLE_DAY is not enaugh to meet duration requirement
    if(optionalImportantRanges.size() == 0){
      if(request.getDuration() <= TimeRange.WHOLE_DAY.duration()){
        return Arrays.asList(TimeRange.WHOLE_DAY);
      } else{ 
        return Arrays.asList();
      }
    }

    //sorting ranges by start to ease proces of merging intervals
    optionalImportantRanges.sort(TimeRange.ORDER_BY_START);
    importantRanges.sort(TimeRange.ORDER_BY_START);
    
    //merging intervals in both importantRanges and optionalImportantRanges
    TimeRange popedRange;
    if(importantRanges.size() > 0){
      busyRanges.push(importantRanges.get(0));
      for(TimeRange timeRange : importantRanges){
        if(timeRange.overlaps(busyRanges.peek())){
          popedRange = busyRanges.pop();
          busyRanges.push(mergeTimeRanges(popedRange, timeRange));
        }else{
          busyRanges.push(timeRange);
        }
      }
    }
  
    optionalBusyRanges.push(optionalImportantRanges.get(0));
    for(TimeRange timeRange : optionalImportantRanges){
      if(timeRange.overlaps(optionalBusyRanges.peek())){
        popedRange = optionalBusyRanges.pop();
        optionalBusyRanges.push(mergeTimeRanges(popedRange, timeRange));
      }else{
        optionalBusyRanges.push(timeRange);
      }
    }

    //looking for free spaces among merged intervals
    int duration;
    //dummy TimeRanges to provide limits to the entire space
    busyRanges.add(0, new TimeRange(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY));
    busyRanges.add(new TimeRange(TimeRange.END_OF_DAY+1, TimeRange.END_OF_DAY+1));
    optionalBusyRanges.add(0, new TimeRange(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY));
    optionalBusyRanges.add(new TimeRange(TimeRange.END_OF_DAY+1, TimeRange.END_OF_DAY+1));
    //this loop calculates size of free space between two subsequent intervals, and checks if size meets requirements
    //if so it adds this space to freeIntervals that stores possible place for requested event
    for(int i = 0; i < busyRanges.size()-1; i++){
      duration = busyRanges.get(i+1).start()-busyRanges.get(i).end();
      if(duration > 0 && duration >= request.getDuration())
        freeRanges.add(new TimeRange(busyRanges.get(i).end(), busyRanges.get(i+1).start()-busyRanges.get(i).end()));
    }
    for(int i = 0; i < optionalBusyRanges.size()-1; i++){
      duration = optionalBusyRanges.get(i+1).start()-optionalBusyRanges.get(i).end();
      if(duration > 0 && duration >= request.getDuration())
        freeRangesWithOptionals.add(new TimeRange(optionalBusyRanges.get(i).end(), optionalBusyRanges.get(i+1).start()-optionalBusyRanges.get(i).end()));
    }

    //if with optional attendees there is no room for the meeting we return freeRangesWithoutOptionals
    if(freeRangesWithOptionals.size() == 0) return freeRanges;
    else return freeRangesWithOptionals;
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

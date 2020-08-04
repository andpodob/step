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

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> busyIntervals = new ArrayList<TimeRange>();
    ArrayList<TimeRange> freeIntervals = new ArrayList<TimeRange>();
    ArrayList<TimeRange> importantRanges = new ArrayList<TimeRange>();
    
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
      if(busyIntervals.size() > 0 && timeRange.overlaps(busyIntervals.get(busyIntervals.size()-1))){
        popedRange = busyIntervals.get(busyIntervals.size()-1);
        busyIntervals.remove(busyIntervals.size() - 1);
        busyIntervals.add(mergeTimeRanges(popedRange, timeRange));
      }else{
        busyIntervals.add(timeRange);
      }
    }

    int duration;
    if(busyIntervals.size() > 0 ) {
      //interval in the beginig
      duration = busyIntervals.get(0).start();
      if(duration > 0 && duration >= request.getDuration()) 
        freeIntervals.add(new TimeRange(TimeRange.START_OF_DAY, busyIntervals.get(0).start()));
      
      //intervals in the middle
      for(int i = 0; i < busyIntervals.size()-1; i++){
        duration = busyIntervals.get(i+1).start()-busyIntervals.get(i).end();
        if(duration > 0 && duration >= request.getDuration())
          freeIntervals.add(new TimeRange(busyIntervals.get(i).end(), busyIntervals.get(i+1).start()-busyIntervals.get(i).end()));
      }
      
      //interval at the end
      duration = TimeRange.END_OF_DAY - busyIntervals.get(busyIntervals.size()-1).end()+1;
      if(duration > 0 && duration >= request.getDuration())
        freeIntervals.add(new TimeRange(busyIntervals.get(busyIntervals.size()-1).end(), TimeRange.END_OF_DAY - busyIntervals.get(busyIntervals.size()-1).end()+1));
    
    } else if(request.getDuration() <= TimeRange.WHOLE_DAY.duration()){
      freeIntervals.add(TimeRange.WHOLE_DAY);
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

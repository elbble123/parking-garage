package nl.cityparking.garfield.simulator.agent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Schedule contains data about which days the agent works, which aids in
 * deciding when they arrive at the garage and when they leave.
 *
 * @author Jesse
 * @since 1.0
 */
public class Schedule {
	private ArrayList<Entry> schedule = new ArrayList<>();

	/**
	 * Adds an entry to the work schedule of this Schedule.
	 * @param hours The Entry to be added to the schedule.
	 */
	public void addWorkingHours(Entry hours) {
		// TODO: Check if they overlap
		schedule.add(hours);
		Collections.sort(schedule);
	}

	/**
	 * Returns whether or not the hour of the week is a working hour.
	 * @param hourOfWeek The current hour of the week.
	 * @return True if working, false if not working.
	 */
	public boolean isWorking(long hourOfWeek) {
		for (Entry hours : schedule) {
			if (hourOfWeek >= hours.startHour && hourOfWeek < hours.endHour) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Calculates if there's a working hour between the begin and end hour.
	 * @param begin A hour of the week (value between 0 and 167)
	 * @param end A hour of the week (value between 0 and 167)
	 * @return True if there's an entry in the schedule between the given hours, false if not.
	 */
	public boolean worksBetween(long begin, long end) {
		for (Entry h: schedule) {
			if (h.startHour >= begin && h.startHour <= end) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the next hour in the employment schedule given on the current hour.
	 * @param currentHour The current hour of the week.
	 * @return The next hour of week on which a work schedule starts.
	 */
	public Entry getNextWorkHour(long currentHour) {
		for (Entry h: schedule) {
			if (h.startHour >= currentHour) {
				return h;
			}
		}

		return null;
	}

	public final ArrayList<Entry> getSchedule() {
		return schedule;
	}

	/**
	 * Entry shows an entry in the Schedule's schedule, showing when an Agent has to work or not.
	 *
	 * @author Jesse
	 * @since 1.0
	 */
	public static class Entry implements Comparable<Entry> {
		private long startHour = 0;
		private long endHour = 0;

		/**
		 * Creates a new Entry. If the starting hour is greater than the ending hour, this function
		 * will fail dramatically.
		 * @param startHour The starting hour of the week.
		 * @param endHour The ending hour of the week.
		 */
		public Entry(long startHour, long endHour) {
			startHour = limit(startHour);
			endHour = limit(endHour);
			assert (startHour < endHour);

			this.startHour = startHour;
			this.endHour = endHour;
		}

		/**
		 * Clamps a value so that it becomes a hour of the week between 0 and 167.
		 * @param hour A hour.
		 * @return The hour as a hour of the week (0 to 167)
		 */
		public static long limit(long hour) {
			return hour % (24 * 7);
		}

		/**
		 * Gets the starting hour of this entry.
		 * @return The hour of the week upon which this entry starts.
		 */
		public long getStartHour() {
			return startHour;
		}

		/**
		 * Gets the ending hour of this entry.
		 * @return The hour of the week upon which this entry ends.
		 */
		public long getEndHour() {
			return endHour;
		}

		@Override
		public int compareTo(@NotNull Schedule.Entry o) {
			return Long.compare(endHour, o.startHour);
		}
	}
}

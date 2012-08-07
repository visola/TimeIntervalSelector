package com.bearprogrammer.time;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Represent an interval between two instants of time.
 * 
 * @author Vinicius Isola
 */
public class TimeInterval implements Cloneable, Comparable<TimeInterval> {

	protected Calendar start, end;
	
	private Set<ChangeListener> listeners = new HashSet<ChangeListener>();
	
	/**
	 * Create a new instance of this class, where start and end are set to now.
	 */
	public TimeInterval() {
		this(null, null);
	}
	
	/**
	 * Create a new instance of this class.
	 * 
	 * @param start
	 *            The beginning of the interval.
	 * @param end
	 *            The ending of the interval.
	 * @throws IllegalArgumentException
	 *             If start is after end.
	 */
	public TimeInterval(Calendar start, Calendar end)
			throws IllegalArgumentException {
		if (start == null) {
			start = Calendar.getInstance();
		}

		if (end == null) {
			end = (Calendar) start.clone();
		}
		setStart(start);
		setEnd(end);
	}

	public void addChangeListener(ChangeListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	/**
	 * Add an amount of milliseconds to the end of this interval.
	 * 
	 * @param milliseconds
	 *            The amount of milliseconds to add, positive or negative.
	 */
	public void addToEndDate(int milliseconds) {
		end.add(Calendar.MILLISECOND, milliseconds);
		notifyListeners();
	}

	/**
	 * Add an amount of milliseconds to the beginning of the this interval. Note
	 * that adding a positive amount will shorten the interval because will push
	 * the start forward in time.
	 * 
	 * @param milliseconds
	 *            The amount of milliseconds to add, positive or negative.
	 */
	public void addToStartDate(int milliseconds) {
		start.add(Calendar.MILLISECOND, milliseconds);
		notifyListeners();
	}

	@Override
	public Object clone() {
		TimeInterval result = new TimeInterval();
		result.start = (Calendar) start.clone();
		result.end = (Calendar) end.clone();
		
		return result;
	}

	public int compareTo(TimeInterval toCompare) {
		return start.compareTo(toCompare.start);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeInterval other = (TimeInterval) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	/**
	 * Get the duration of this interval.
	 * 
	 * @return The duration of the interval in milliseconds.
	 */
	public long getDuration() {
		return end.getTimeInMillis() - start.getTimeInMillis();
	}

	/**
	 * The end of this interval as a Calendar. Note that changing the object
	 * retrieved by this method will take no effect on the interval.
	 * 
	 * @return A Calendar representation of the end of this interval.
	 */
	public Calendar getEnd() {
		return (Calendar) end.clone();
	}

	/**
	 * The start of this interval as a Calendar. Note that changing the object
	 * retrieved by this method will take no effect on the interval.
	 * 
	 * @return A Calendar representation of the start of this interval.
	 */
	public Calendar getStart() {
		return (Calendar) start.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	protected void notifyListeners() {
		ChangeEvent event = new ChangeEvent(this);
		for (ChangeListener l : listeners) {
			l.stateChanged(event);
		}
	}

	public void removeChangeListener(ChangeListener listener) {
		if (listener == null) return;
		listeners.remove(listener);
	}

	/**
	 * Set the end of this interval using a Calendar object. Note that the
	 * object will not be stored internally, changing it after calling this
	 * method will have no effect on the interval.
	 * 
	 * @param end
	 *            The new end for this interval.
	 * @throws IllegalArgumentException
	 *             If the new ending happens to be before the beginning of the
	 *             interval.
	 * @throws NullPointerException
	 *             If <code>end</code> is null.
	 */
	public void setEnd(Calendar end) throws IllegalArgumentException,
			NullPointerException {

		if (end == null) {
			throw new NullPointerException(
					"Can not set the interval to a null end.");
		}

		if (start != null && end.compareTo(start) < 0) {
			throw new IllegalArgumentException("Start must be before end.");
		}

		this.end = (Calendar) end.clone();
		notifyListeners();
	}

	/**
	 * Change the day of the ending of this interval.
	 * 
	 * @param day
	 *            The new ending day.
	 * @throws IllegalArgumentException
	 *             If the new ending happens to be before the beginning of the
	 *             interval.
	 */
	public void setEndDate(int day) throws IllegalArgumentException {
		setEndDate(end.get(Calendar.YEAR), end.get(Calendar.MONTH), day);
	}

	/**
	 * Change the day and month of the ending of this interval.
	 * 
	 * @param month
	 *            The new month.
	 * @param day
	 *            The new day.
	 * @throws IllegalArgumentException
	 *             If the new ending happens to be before the beginning of the
	 *             interval.
	 */
	public void setEndDate(int month, int day) throws IllegalArgumentException {
		setEndDate(end.get(Calendar.YEAR), month, day);
	}

	/**
	 * Change the ending date of this interval.
	 * 
	 * @param year
	 *            The new year.
	 * @param month
	 *            The new month.
	 * @param day
	 *            The new day.
	 * @throws IllegalArgumentException
	 *             If the new ending happens to be before the beginning of the
	 *             interval.
	 */
	public void setEndDate(int year, int month, int day)
			throws IllegalArgumentException {
		Calendar newEnd = (Calendar) end.clone();
		newEnd.set(Calendar.YEAR, year);
		newEnd.set(Calendar.MONTH, month - 1);
		newEnd.set(Calendar.DAY_OF_MONTH, day);

		if (newEnd.compareTo(start) < 0) {
			throw new IllegalArgumentException("Start must be before end.");
		}

		end = newEnd;
		notifyListeners();
	}

	/**
	 * Change the time for the ending of this interval.
	 * 
	 * @param hour
	 *            The new hour.
	 * @param minute
	 *            The new minutes.
	 * @throws IllegalArgumentException
	 *             If the new ending happens to be before the beginning of the
	 *             interval.
	 */
	public void setEndTime(int hour, int minute)
			throws IllegalArgumentException {
		Calendar newEnd = (Calendar) end.clone();
		newEnd.set(Calendar.HOUR_OF_DAY, hour);
		newEnd.set(Calendar.MINUTE, minute);

		System.out.println(start.get(Calendar.HOUR_OF_DAY) + " - " + newEnd.get(Calendar.HOUR_OF_DAY));
		if (newEnd.compareTo(start) < 0) {
			throw new IllegalArgumentException("Start must be before end.");
		}

		end = newEnd;
		notifyListeners();
	}

	/**
	 * Set the start of this interval using a Calendar object. Note that the
	 * object will not be stored internally, changing it after calling this
	 * method will have no effect on the interval.
	 * 
	 * @param end
	 *            The new start for this interval.
	 * @throws IllegalArgumentException
	 *             If the new beginning happens to be after the end of the
	 *             interval.
	 * @throws NullPointerException
	 *             If <code>start</code> is null.
	 */
	public void setStart(Calendar start) throws IllegalArgumentException, NullPointerException {
		if (start == null) {
			throw new NullPointerException(
					"Can not set interval to a null start.");
		}

		if (end != null && start.compareTo(end) > 0) {
			throw new IllegalArgumentException("End must be after start.");
		}

		this.start = (Calendar) start.clone();
		notifyListeners();
	}

	/**
	 * Change the start date of this interval.
	 * 
	 * @param day
	 *            The new day.
	 * @throws IllegalArgumentException
	 *             If the new beginning happens to be after the end of the
	 *             interval.
	 */
	public void setStartDate(int day) throws IllegalArgumentException {
		setStartDate(end.get(Calendar.YEAR), end.get(Calendar.MONTH), day);
	}

	/**
	 * Change the start date of this interval.
	 * 
	 * @param month
	 *            The new month.
	 * @param day
	 *            The new day.
	 * @throws IllegalArgumentException
	 *             If the new beginning happens to be after the end of the
	 *             interval.
	 */
	public void setStartDate(int month, int day)
			throws IllegalArgumentException {
		setStartDate(end.get(Calendar.YEAR), month, day);
	}

	/**
	 * Change the start date of this interval.
	 * 
	 * @param year
	 *            The new year.
	 * @param month
	 *            The new month.
	 * @param day
	 *            The new day.
	 * @throws IllegalArgumentException
	 *             If the new beginning happens to be after the end of the
	 *             interval.
	 */
	public void setStartDate(int year, int month, int day)
			throws IllegalArgumentException {
		Calendar newStart = (Calendar) start.clone();
		newStart.set(Calendar.YEAR, year);
		newStart.set(Calendar.MONTH, month - 1);
		newStart.set(Calendar.DAY_OF_MONTH, day);

		if (newStart.compareTo(end) > 0) {
			throw new IllegalArgumentException("End must be after start.");
		}

		start = newStart;
		notifyListeners();
	}

	/**
	 * Change the time for the beginning of this interval.
	 * 
	 * @param hour
	 *            The new hour.
	 * @param minute
	 *            The new minutes.
	 * @throws IllegalArgumentException
	 *             If the new beginning happens to be after the end of the
	 *             interval.
	 */
	public void setStartTime(int hour, int minute)
			throws IllegalArgumentException {
		Calendar newStart = (Calendar) start.clone();
		newStart.set(Calendar.HOUR_OF_DAY, hour);
		newStart.set(Calendar.MINUTE, minute);

		if (newStart.compareTo(end) > 0) {
			throw new IllegalArgumentException("End must be after start.");
		}

		start = newStart;
		notifyListeners();
	}

}
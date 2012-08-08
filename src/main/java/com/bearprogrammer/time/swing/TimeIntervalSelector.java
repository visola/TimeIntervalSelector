/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2012 Vinicius Isola
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 */
package com.bearprogrammer.time.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bearprogrammer.time.TimeInterval;


/**
 * Time interval selection tool is used to select two points in time that
 * represent a {@link TimeInterval}.
 * 
 * @author Vinicius Isola
 */
public class TimeIntervalSelector extends JPanel implements ChangeListener, MouseListener, MouseMotionListener, MouseWheelListener {

	/**
	 * Enumeration that describes what was clicked and what will be dragged by
	 * the user.
	 */
	protected static enum ObjectClicked {
		COMPONENT, INTERVAL_BODY, START_KNOB, END_KNOB
	}

	private static final long serialVersionUID = -8442234454653526282L;

	/**
	 * For testing purpose only. Demonstrate the normal functioning of this
	 * component.
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame("Teste");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		TimeIntervalSelector selector = new TimeIntervalSelector();
		selector.setDisplayInterval(8 * 60);
		f.add(selector);

		Calendar today = Calendar.getInstance();

		Calendar s1 = (Calendar) today.clone();
		s1.set(Calendar.HOUR_OF_DAY, 8);
		s1.set(Calendar.MINUTE, 30);

		Calendar e1 = (Calendar) today.clone();
		e1.set(Calendar.HOUR_OF_DAY, 10);
		e1.set(Calendar.MINUTE, 0);

		TimeInterval t = new TimeInterval(s1, e1);
		selector.setInterval(t);

		f.pack();
		f.setVisible(true);
	}

	/**
	 * Color used to fill the knobs.
	 */
	public Color COLOR_KNOBS = new Color(0.2F, 0.3F, 0.4F);

	/**
	 * Color used fill the interval.
	 */
	public Color COLOR_INTERVAL = new Color(0.8F, 0.9F, 1F, 0.8F);

	/**
	 * Color used to draw the interval border.
	 */
	public Color COLOR_INTERVAL_BORDER = new Color(50, 50, 50);

	/**
	 * Color used to draw the interval text, e.g. start and end times.
	 */
	public static final Color COLOR_INTERVAL_TEXT = null;

	/** Color used to draw the hour background line. */
	public Color COLOR_HOUR_LINE = new Color(150, 150, 150);

	/**
	 * Color used to draw the half-hour background line.
	 */
	public Color COLOR_HALF_HOUR_LINE = new Color(180, 180, 180);

	/**
	 * Color used to draw the day background line.
	 */
	public Color COLOR_DAY_LINE = new Color(100, 100, 100);

	/**
	 * Font used to draw the string that shows the date.
	 */
	protected static final Font DATE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 10);

	/**
	 * Font used to draw the string that shows the hours.
	 */
	protected static final Font HOUR_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

	/**
	 * Font used to draw the string that shows the start and end time of the
	 * interval.
	 */
	protected static final Font TIME_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 8);

	/**
	 * Minimal amount that can be displayed by this component, in minutes.
	 */
	protected static final int MIN_DISPLAY_INTERVAL = 3 * 60;

	/**
	 * Maximum amount that can be displayed by this component, in minutes.
	 */
	protected static final int MAX_DISPLAY_INTERVAL = 24 * 60;

	/**
	 * Formatter used to format dates.
	 */
	protected SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

	/**
	 * Formatter used to format time.
	 */
	protected SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm");

	/** The least amount of minutes the user can move an <code>interval</code>. */
	protected int gridInterval = 15 * 60 * 1000;

	/**
	 * The beginning of the display area.
	 */
	protected Calendar start;

	/**
	 * The end of the display area.
	 */
	protected Calendar end;

	/**
	 * Interval that is being manipulated in this component.
	 */
	protected TimeInterval interval;

	/**
	 * Size of the display area, in minutes.
	 */
	protected int displayInterval = 6 * 60;

	protected Rectangle startKnob, endKnob, body;

	/**
	 * Store the object that was clicked.
	 */
	private ObjectClicked clickedOn = null;

	/**
	 * The last relevant <code>MouseEvent</code> that was executed in this
	 * component.
	 */
	private MouseEvent lastEvent;

	/**
	 * Create a new instance of this class with default display interval time,
	 * starting now and now interval to show.
	 */
	public TimeIntervalSelector() {
		this(null, -1);
	}
	
	/**
	 * Create a new instance of this class with the parameters specified.
	 * 
	 * @param displayInterval
	 *            The interval that will be displayed in this component, in
	 *            minutes.
	 */
	public TimeIntervalSelector(int displayInterval) {
		this(null, displayInterval);
	}
	
	/**
	 * Create a new instance of this class with the parameters specified.
	 * 
	 * @param interval
	 *            The interval to set in this component, null to ignore it.
	 * @param displayInterval
	 *            The interval that will be displayed in this component, in
	 *            minutes.
	 */
	public TimeIntervalSelector(TimeInterval interval, int displayInterval) {
		setPreferredSize(new Dimension(400, 35));
		setMinimumSize(getPreferredSize());
		setBackground(Color.WHITE);
		
		registerListeners();

		start = Calendar.getInstance();

		if (displayInterval == -1)
			displayInterval = this.displayInterval;
		refreshDisplayInterval();

		if (interval != null) {
			setInterval(interval);
		}
	}

	private void registerListeners() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	private void unregisterListeners() {
		removeMouseListener(this);
		removeMouseMotionListener(this);
		removeMouseWheelListener(this);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		if (enabled) {
			registerListeners();
			setBackground(Color.WHITE);
		} else {
			unregisterListeners();
			setBackground(new Color(235, 235, 235));
		}
	}

	/**
	 * Return the amount of minutes that is being displayed by this component.
	 * 
	 * @return The amount of time displayed, in minutes.
	 */
	public int getDisplayInterval() {
		return displayInterval;
	}

	public int getGridInterval() {
		return gridInterval;
	}

	/**
	 * Return the {@link TimeInterval} that is set in this component.
	 * 
	 * @return The interval selected.
	 */
	public TimeInterval getInterval() {
		return interval;
	}

	/**
	 * Return the time that represent the beginning of the displayable area of
	 * this component.
	 * 
	 * @return The beginning of the displayable area.
	 */
	public Calendar getStartingAt() {
		return (Calendar) start.clone();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// Do nothing
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (lastEvent == null)
			return;

		int offsetX = lastEvent.getX() - e.getX();

		long showInterval = end.getTimeInMillis() - start.getTimeInMillis();
		long offsetXInMillis = showInterval * offsetX / getWidth();

		// Only proceed if the interval is greater than 15 minutes
		if (offsetXInMillis <= gridInterval && offsetXInMillis >= -gridInterval) {
			return;
		}

		offsetXInMillis = (int) (offsetXInMillis / gridInterval) * gridInterval;

		Calendar intervalStart, intervalEnd;
		
		switch (clickedOn) {
		case INTERVAL_BODY:
			interval.addToStartDate(-(int) offsetXInMillis);
			interval.addToEndDate(-(int) offsetXInMillis);
			
			intervalStart = interval.getStart(); 
			if (intervalStart.before(start)) {
				start.add(Calendar.MILLISECOND, - (int) offsetXInMillis);
				refreshDisplayInterval();
			}
			
			intervalEnd = interval.getEnd();
			if (intervalEnd.after(end)) {
				start.add(Calendar.MILLISECOND, - (int) offsetXInMillis);
				refreshDisplayInterval();
			}
			
			break;
		case START_KNOB:
			interval.addToStartDate(-(int) offsetXInMillis);
			
			intervalStart = interval.getStart(); 
			if (intervalStart.before(start)) {
				start.add(Calendar.MILLISECOND, - (int) offsetXInMillis);
				refreshDisplayInterval();
			}
			
			break;
		case END_KNOB:
			interval.addToEndDate(-(int) offsetXInMillis);
			
			intervalEnd = interval.getEnd();
			if (intervalEnd.after(end)) {
				start.add(Calendar.MILLISECOND, - (int) offsetXInMillis);
				refreshDisplayInterval();
			}
			
			break;
		case COMPONENT:
			start.add(Calendar.MILLISECOND, (int) offsetXInMillis);
			end.add(Calendar.MILLISECOND, (int) offsetXInMillis);
			break;
		}

		lastEvent = e;

		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (startKnob != null && startKnob.contains(e.getPoint())) {
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		} else if (endKnob != null && endKnob.contains(e.getPoint())) {
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		} else if (body != null && body.contains(e.getPoint())) {
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		lastEvent = e;

		if (startKnob != null && startKnob.contains(e.getPoint())) {
			clickedOn = ObjectClicked.START_KNOB;
		} else if (endKnob != null && endKnob.contains(e.getPoint())) {
			clickedOn = ObjectClicked.END_KNOB;
		} else if (body != null && body.contains(e.getPoint())) {
			clickedOn = ObjectClicked.INTERVAL_BODY;
		} else {
			clickedOn = ObjectClicked.COMPONENT;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		lastEvent = null;
		clickedOn = null;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int newDisplayValue = this.displayInterval + 15 * e.getWheelRotation();
		if (newDisplayValue < MIN_DISPLAY_INTERVAL || newDisplayValue > MAX_DISPLAY_INTERVAL) {
			return;
		}
		setDisplayInterval(newDisplayValue);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paint(g);
		
		if (!isEnabled()) return;

		// Store the dimension values
		int border = 1;
		float componentX = border;
		float componentY = border;
		float componentWidth = getWidth() - 2 * border;
		float componentHeight = getHeight() - 2 * border;

		// Start and End showing time as minutes
		float startShowing = start.getTimeInMillis() / (60 * 1000);
		float stopShowing = end.getTimeInMillis() / (60 * 1000);

		// --- Draw half hour, hour and day lines---
		// Check where to start from
		long startLinesFrom = (long) startShowing;
		int minutesCounter = 0;
		while (startLinesFrom % 30 != 0) {
			startLinesFrom--;
			minutesCounter--;
		}

		// Calendar object to control time
		Calendar lineHour = Calendar.getInstance();
		lineHour.setTimeInMillis(startLinesFrom * 60 * 1000);
		int day = lineHour.get(Calendar.DAY_OF_MONTH);
		int textStartY = 20;
		int dateStartY = 10;

		int nextLine = minutesCounter;
		int dayLinePosition = 5;
		for (int i = 0; i <= (int) ((stopShowing - startShowing) / 30); i++) {
			int startY = (int) componentY;
			boolean drawString = true;

			// Calculate the position of the line
			int posX = (int) componentX + (int) (componentWidth * nextLine / (stopShowing - startShowing));

			// draw day line
			if (lineHour.get(Calendar.DAY_OF_MONTH) != day) {
				g2.setColor(COLOR_DAY_LINE);
				day = lineHour.get(Calendar.DAY_OF_MONTH);
				dayLinePosition = posX + 2;

				// draw hour line
			} else if (lineHour.get(Calendar.MINUTE) == 0) {
				g2.setColor(COLOR_HOUR_LINE);

				// draw half-hour line
			} else {
				startY = (int) componentY + dateStartY + 1;
				g2.setColor(COLOR_HALF_HOUR_LINE);
				drawString = false;
			}

			// Draw the line (half hour, hour or day)
			g2.drawLine(posX, startY, posX, (int) componentY + (int) componentHeight);

			// Draw the hour for hour lines
			if (drawString) {
				g2.setFont(HOUR_FONT);
				g2.drawString(Integer.toString(lineHour.get(Calendar.HOUR_OF_DAY)), posX + 2, (int) (componentY) + textStartY);
			}

			// Add 30 minutes to the lines that are being drawn
			nextLine += 30;
			lineHour.add(Calendar.MINUTE, 30);
		}

		// Draw the date
		g2.setColor(COLOR_DAY_LINE);
		g2.setFont(DATE_FONT);
		g2.drawString(DATE_FORMATTER.format(lineHour.getTime()), dayLinePosition, (int) (componentY) + dateStartY);

		// -- End drawing lines --

		// -- Draw the interval if there is one
		if (interval != null) {
			Calendar intervalStart = interval.getStart();
			Calendar intervalEnd = interval.getEnd();

			// Check to see if the interval is visible
			if ((intervalStart.before(end) && intervalEnd.after(start)) || (intervalEnd.before(end) && intervalEnd.after(start))) {

				// Calculate its size
				int startingAt = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE);

				int startPos = intervalStart.get(Calendar.HOUR_OF_DAY) * 60 + intervalStart.get(Calendar.MINUTE) - startingAt + 1;
				if (start.get(Calendar.DAY_OF_MONTH) != intervalStart.get(Calendar.DAY_OF_MONTH)) {
					startPos += 24 * 60;
				}

				int endPos = intervalEnd.get(Calendar.HOUR_OF_DAY) * 60 + intervalEnd.get(Calendar.MINUTE) - startingAt + 1;
				if (start.get(Calendar.DAY_OF_MONTH) != intervalEnd.get(Calendar.DAY_OF_MONTH)) {
					endPos += 24 * 60;
				}

				int posXStart = (int) (componentWidth * startPos / (stopShowing - startShowing));
				int posXEnd = (int) (componentWidth * endPos / (stopShowing - startShowing));
				int posYStart = dateStartY + 2;

				body = new Rectangle(posXStart, posYStart, posXEnd - posXStart, getHeight());
				g2.setColor(COLOR_INTERVAL);
				g2.fill(body);

				g2.setColor(COLOR_INTERVAL_BORDER);
				g2.draw(body);

				g2.setColor(COLOR_KNOBS);
				startKnob = new Rectangle(posXStart, posYStart, 5, getHeight());
				g2.fill(startKnob);

				endKnob = new Rectangle(posXEnd - 4, posYStart, 5, getHeight());
				g2.fill(endKnob);

				// Draw the start and end time
				String startTime = TIME_FORMATTER.format(intervalStart.getTime());
				String endTime = TIME_FORMATTER.format(intervalEnd.getTime());

				FontMetrics metrics = g2.getFontMetrics(TIME_FONT);

				g2.setColor(COLOR_INTERVAL_TEXT);
				g2.drawString(startTime, posXStart + startKnob.width + 2, posYStart + metrics.getHeight());
				g2.drawString(endTime, posXStart + body.width - metrics.stringWidth(endTime) - 15, posYStart + 2 * metrics.getHeight());
			}
		}
		// -- End drawing interval
		
		
		paintBorder(g2);
	}

	/**
	 * Refresh the display area after changing the beginning of the displayable
	 * area.
	 */
	protected void refreshDisplayInterval() {
		end = (Calendar) start.clone();
		end.add(Calendar.MINUTE, displayInterval);
		repaint();
	}

	/**
	 * Change the displayable area size.
	 * 
	 * @param displayInterval
	 *            The new amount of minutes to be shown.
	 */
	public void setDisplayInterval(int displayInterval) {
		if (displayInterval < MIN_DISPLAY_INTERVAL) {
			throw new IllegalArgumentException("Display interval must not be less than 3 hours.");
		}
		if (displayInterval > MAX_DISPLAY_INTERVAL) {
			throw new IllegalArgumentException("Display interval must not be greater than 24 hours.");
		}

		this.displayInterval = displayInterval;
		refreshDisplayInterval();
	}

	public void setGridInterval(int gridInterval) {
		this.gridInterval = gridInterval;
	}

	/**
	 * Change the interval selection for this component.
	 * 
	 * @param interval
	 *            The new interval.
	 */
	public void setInterval(TimeInterval interval) {
		if (this.interval != null) {
			this.interval.removeChangeListener(this);
		}

		this.interval = interval;
		if (this.interval != null) {
			this.interval.addChangeListener(this);
		}

		showInterval();
	}

	/**
	 * When for some reason the interval is not in the displayable area, calling
	 * this method will set the beginning of the displayable area to one hour
	 * before the {@link TimeInterval#getStart() interval start}.
	 */
	public void showInterval() {
		if (interval == null)
			return;
		start = (Calendar) interval.getStart().clone();
		start.add(Calendar.HOUR_OF_DAY, -1);
		refreshDisplayInterval();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		repaint();
	}

}
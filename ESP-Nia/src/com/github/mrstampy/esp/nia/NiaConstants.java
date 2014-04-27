/*
 * ESP-Nia Copyright (C) 2014 Burton Alexander
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 */
package com.github.mrstampy.esp.nia;

public interface NiaConstants {

	public static final short NIA_VENDOR = (short) 0x1234;
	public static final short NIA_DEVICE = (short) 0x0;
	public static final int NIA_ENDPOINT_1 = 0x81;
	public static final int NIA_ENDPOINT_2 = 0x1;

	/**
	 * During testing the device consistently returned 3906 samples / second
	 * to 4 ten thousandths of a second.
	 */
	public static final int BUFFER_SIZE = 3906;
	public static final int FFT_SIZE = 2048;
	public static final double SAMPLE_RATE = 100;
	public static final double SAMPLE_SLEEP = 1000 / SAMPLE_RATE;
	
	public static final double LOWEST_SIGNAL_VAL = -8388608;
	public static final double HIGHEST_SIGNAL_VAL = 8388607;

}

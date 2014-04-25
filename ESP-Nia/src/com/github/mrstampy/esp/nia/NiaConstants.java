package com.github.mrstampy.esp.nia;

public interface NiaConstants {

	public static final short NIA_VENDOR = (short) 0x1234;
	public static final short NIA_DEVICE = (short) 0x0;
	public static final int NIA_ENDPOINT_1 = 0x81;
	public static final int NIA_ENDPOINT_2 = 0x1;

	/**
	 * During testing the device consistently returned 3910 samples / second
	 * to 4 ten thousandths of a second.
	 */
	public static final int BUFFER_SIZE = 3910;
	public static final int FFT_SIZE = 512;
	public static final double SAMPLE_RATE = 100;
	public static final double SAMPLE_SLEEP = 1000 / SAMPLE_RATE;
	
	public static final double LOWEST_SIGNAL_VAL = -8388608;
	public static final double HIGHEST_SIGNAL_VAL = 8388607;

}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.Neomoon.dronebox.LUA.LUAObjects;

import java.math.BigDecimal;
import java.util.Random;


public final class LUAMath {
	public final double E = 2.718281828459045;
	public final double PI = Math.PI;
	public final double TAU = (PI * 2D);
	private final double DEGREES_TO_RADIANS = (PI / 180D);
	private final double RADIANS_TO_DEGREES = (180D / PI);
	private final long negativeZeroFloatBits = (long)Float.floatToRawIntBits(-0.0F);
	private final long negativeZeroDoubleBits = Double.doubleToRawLongBits((double)-0.0F);
	double twoToTheDoubleScaleUp = powerOfTwoD(512);
	double twoToTheDoubleScaleDown = powerOfTwoD(-512);

	public LUAMath() {
	}


	public double sin(double a) {
		return StrictMath.sin(a);
	}


	public double cos(double a) {
		return StrictMath.cos(a);
	}


	public double tan(double a) {
		return StrictMath.tan(a);
	}

	public double asin(double a) {
		return StrictMath.asin(a);
	}

	public double acos(double a) {
		return StrictMath.acos(a);
	}

	public double atan(double a) {
		return StrictMath.atan(a);
	}

	public double toRadians(double angdeg) {
		return angdeg * (PI / 180D);
	}

	public double toDegrees(double angrad) {
		return angrad * (180D / PI);
	}


	public double exp(double a) {
		return StrictMath.exp(a);
	}


	public double log(double a) {
		return StrictMath.log(a);
	}


	public double log10(double a) {
		return StrictMath.log10(a);
	}


	public double sqrt(double a) {
		return StrictMath.sqrt(a);
	}

	public double cbrt(double a) {
		return StrictMath.cbrt(a);
	}

	public double IEEEremainder(double f1, double f2) {
		return StrictMath.IEEEremainder(f1, f2);
	}


	public double ceil(double a) {
		return StrictMath.ceil(a);
	}


	public double floor(double a) {
		return StrictMath.floor(a);
	}


	public double rint(double a) {
		return StrictMath.rint(a);
	}


	public double atan2(double y, double x) {
		return StrictMath.atan2(y, x);
	}


	public double pow(double a, double b) {
		return StrictMath.pow(a, b);
	}


	public int round(float a) {
		int intBits = Float.floatToRawIntBits(a);
		int biasedExp = (intBits & 2139095040) >> 23;
		int shift = 149 - biasedExp;
		if ((shift & -32) == 0) {
			int r = intBits & 8388607 | 8388608;
			if (intBits < 0) {
				r = -r;
			}

			return (r >> shift) + 1 >> 1;
		} else {
			return (int)a;
		}
	}


	public long round(double a) {
		long longBits = Double.doubleToRawLongBits(a);
		long biasedExp = (longBits & 9218868437227405312L) >> 52;
		long shift = 1074L - biasedExp;
		if ((shift & -64L) == 0L) {
			long r = longBits & 4503599627370495L | 4503599627370496L;
			if (longBits < 0L) {
				r = -r;
			}

			return (r >> (int)shift) + 1L >> 1;
		} else {
			return (long)a;
		}
	}

	public double random() {
		return LUAMath.RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble();
	}


	public int addExact(int x, int y) {
		int r = x + y;
		if (((x ^ r) & (y ^ r)) < 0) {
			throw new ArithmeticException("integer overflow");
		} else {
			return r;
		}
	}


	public long addExact(long x, long y) {
		long r = x + y;
		if (((x ^ r) & (y ^ r)) < 0L) {
			throw new ArithmeticException("long overflow");
		} else {
			return r;
		}
	}


	public int subtractExact(int x, int y) {
		int r = x - y;
		if (((x ^ y) & (x ^ r)) < 0) {
			throw new ArithmeticException("integer overflow");
		} else {
			return r;
		}
	}


	public long subtractExact(long x, long y) {
		long r = x - y;
		if (((x ^ y) & (x ^ r)) < 0L) {
			throw new ArithmeticException("long overflow");
		} else {
			return r;
		}
	}


	public int multiplyExact(int x, int y) {
		long r = (long)x * (long)y;
		if ((long)((int)r) != r) {
			throw new ArithmeticException("integer overflow");
		} else {
			return (int)r;
		}
	}

	public long multiplyExact(long x, int y) {
		return multiplyExact(x, (long)y);
	}


	public long multiplyExact(long x, long y) {
		long r = x * y;
		long ax = abs(x);
		long ay = abs(y);
		if ((ax | ay) >>> 31 == 0L || (y == 0L || r / y == x) && (x != Long.MIN_VALUE || y != -1L)) {
			return r;
		} else {
			throw new ArithmeticException("long overflow");
		}
	}

	public int divideExact(int x, int y) {
		int q = x / y;
		if ((x & y & q) >= 0) {
			return q;
		} else {
			throw new ArithmeticException("integer overflow");
		}
	}

	public long divideExact(long x, long y) {
		long q = x / y;
		if ((x & y & q) >= 0L) {
			return q;
		} else {
			throw new ArithmeticException("long overflow");
		}
	}

	public int floorDivExact(int x, int y) {
		int q = x / y;
		if ((x & y & q) >= 0) {
			return (x ^ y) < 0 && q * y != x ? q - 1 : q;
		} else {
			throw new ArithmeticException("integer overflow");
		}
	}

	public long floorDivExact(long x, long y) {
		long q = x / y;
		if ((x & y & q) >= 0L) {
			return (x ^ y) < 0L && q * y != x ? q - 1L : q;
		} else {
			throw new ArithmeticException("long overflow");
		}
	}

	public int ceilDivExact(int x, int y) {
		int q = x / y;
		if ((x & y & q) >= 0) {
			return (x ^ y) >= 0 && q * y != x ? q + 1 : q;
		} else {
			throw new ArithmeticException("integer overflow");
		}
	}

	public long ceilDivExact(long x, long y) {
		long q = x / y;
		if ((x & y & q) >= 0L) {
			return (x ^ y) >= 0L && q * y != x ? q + 1L : q;
		} else {
			throw new ArithmeticException("long overflow");
		}
	}


	public int incrementExact(int a) {
		if (a == Integer.MAX_VALUE) {
			throw new ArithmeticException("integer overflow");
		} else {
			return a + 1;
		}
	}


	public long incrementExact(long a) {
		if (a == Long.MAX_VALUE) {
			throw new ArithmeticException("long overflow");
		} else {
			return a + 1L;
		}
	}


	public int decrementExact(int a) {
		if (a == Integer.MIN_VALUE) {
			throw new ArithmeticException("integer overflow");
		} else {
			return a - 1;
		}
	}


	public long decrementExact(long a) {
		if (a == Long.MIN_VALUE) {
			throw new ArithmeticException("long overflow");
		} else {
			return a - 1L;
		}
	}


	public int negateExact(int a) {
		if (a == Integer.MIN_VALUE) {
			throw new ArithmeticException("integer overflow");
		} else {
			return -a;
		}
	}


	public long negateExact(long a) {
		if (a == Long.MIN_VALUE) {
			throw new ArithmeticException("long overflow");
		} else {
			return -a;
		}
	}

	public int toIntExact(long value) {
		if ((long)((int)value) != value) {
			throw new ArithmeticException("integer overflow");
		} else {
			return (int)value;
		}
	}

	public long multiplyFull(int x, int y) {
		return (long)x * (long)y;
	}


	public long multiplyHigh(long x, long y) {
		long x1 = x >> 32;
		long x2 = x & 4294967295L;
		long y1 = y >> 32;
		long y2 = y & 4294967295L;
		long z2 = x2 * y2;
		long t = x1 * y2 + (z2 >>> 32);
		long z1 = t & 4294967295L;
		long z0 = t >> 32;
		z1 += x2 * y1;
		return x1 * y1 + z0 + (z1 >> 32);
	}


	public long unsignedMultiplyHigh(long x, long y) {
		long result = multiplyHigh(x, y);
		result += y & x >> 63;
		result += x & y >> 63;
		return result;
	}

	public int floorDiv(int x, int y) {
		int q = x / y;
		return (x ^ y) < 0 && q * y != x ? q - 1 : q;
	}

	public long floorDiv(long x, int y) {
		return floorDiv(x, (long)y);
	}

	public long floorDiv(long x, long y) {
		long q = x / y;
		return (x ^ y) < 0L && q * y != x ? q - 1L : q;
	}

	public int floorMod(int x, int y) {
		int r = x % y;
		return (x ^ y) < 0 && r != 0 ? r + y : r;
	}

	public int floorMod(long x, int y) {
		return (int)floorMod(x, (long)y);
	}

	public long floorMod(long x, long y) {
		long r = x % y;
		return (x ^ y) < 0L && r != 0L ? r + y : r;
	}

	public int ceilDiv(int x, int y) {
		int q = x / y;
		return (x ^ y) >= 0 && q * y != x ? q + 1 : q;
	}

	public long ceilDiv(long x, int y) {
		return ceilDiv(x, (long)y);
	}

	public long ceilDiv(long x, long y) {
		long q = x / y;
		return (x ^ y) >= 0L && q * y != x ? q + 1L : q;
	}

	public int ceilMod(int x, int y) {
		int r = x % y;
		return (x ^ y) >= 0 && r != 0 ? r - y : r;
	}

	public int ceilMod(long x, int y) {
		return (int)ceilMod(x, (long)y);
	}

	public long ceilMod(long x, long y) {
		long r = x % y;
		return (x ^ y) >= 0L && r != 0L ? r - y : r;
	}


	public int abs(int a) {
		return a < 0 ? -a : a;
	}

	public int absExact(int a) {
		if (a == Integer.MIN_VALUE) {
			throw new ArithmeticException("Overflow to represent absolute value of Integer.MIN_VALUE");
		} else {
			return abs(a);
		}
	}


	public long abs(long a) {
		return a < 0L ? -a : a;
	}

	public long absExact(long a) {
		if (a == Long.MIN_VALUE) {
			throw new ArithmeticException("Overflow to represent absolute value of Long.MIN_VALUE");
		} else {
			return abs(a);
		}
	}


	public float abs(float a) {
		return Float.intBitsToFloat(Float.floatToRawIntBits(a) & Integer.MAX_VALUE);
	}


	public double abs(double a) {
		return Double.longBitsToDouble(Double.doubleToRawLongBits(a) & Long.MAX_VALUE);
	}


	public int max(int a, int b) {
		return a >= b ? a : b;
	}

	public long max(long a, long b) {
		return a >= b ? a : b;
	}


	public float max(float a, float b) {
		if (a != a) {
			return a;
		} else if (a == 0.0F && b == 0.0F && (long)Float.floatToRawIntBits(a) == negativeZeroFloatBits) {
			return b;
		} else {
			return a >= b ? a : b;
		}
	}


	public double max(double a, double b) {
		if (a != a) {
			return a;
		} else if (a == (double)0.0F && b == (double)0.0F && Double.doubleToRawLongBits(a) == negativeZeroDoubleBits) {
			return b;
		} else {
			return a >= b ? a : b;
		}
	}


	public int min(int a, int b) {
		return a <= b ? a : b;
	}

	public long min(long a, long b) {
		return a <= b ? a : b;
	}


	public float min(float a, float b) {
		if (a != a) {
			return a;
		} else if (a == 0.0F && b == 0.0F && (long)Float.floatToRawIntBits(b) == negativeZeroFloatBits) {
			return b;
		} else {
			return a <= b ? a : b;
		}
	}


	public double min(double a, double b) {
		if (a != a) {
			return a;
		} else if (a == (double)0.0F && b == (double)0.0F && Double.doubleToRawLongBits(b) == negativeZeroDoubleBits) {
			return b;
		} else {
			return a <= b ? a : b;
		}
	}

	public int clamp(long value, int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException(min + " > " + max);
		} else {
			return (int)min((long)max, max(value, (long)min));
		}
	}

	public long clamp(long value, long min, long max) {
		if (min > max) {
			throw new IllegalArgumentException(min + " > " + max);
		} else {
			return min(max, max(value, min));
		}
	}

	public double clamp(double value, double min, double max) {
		if (!(min < max)) {
			if (Double.isNaN(min)) {
				throw new IllegalArgumentException("min is NaN");
			}

			if (Double.isNaN(max)) {
				throw new IllegalArgumentException("max is NaN");
			}

			if (Double.compare(min, max) > 0) {
				throw new IllegalArgumentException(min + " > " + max);
			}
		}

		return min(max, max(value, min));
	}

	public float clamp(float value, float min, float max) {
		if (!(min < max)) {
			if (Float.isNaN(min)) {
				throw new IllegalArgumentException("min is NaN");
			}

			if (Float.isNaN(max)) {
				throw new IllegalArgumentException("max is NaN");
			}

			if (Float.compare(min, max) > 0) {
				throw new IllegalArgumentException(min + " > " + max);
			}
		}

		return min(max, max(value, min));
	}


	public double fma(double a, double b, double c) {
		if (!Double.isNaN(a) && !Double.isNaN(b) && !Double.isNaN(c)) {
			boolean infiniteA = Double.isInfinite(a);
			boolean infiniteB = Double.isInfinite(b);
			boolean infiniteC = Double.isInfinite(c);
			if (!infiniteA && !infiniteB && !infiniteC) {
				BigDecimal product = (new BigDecimal(a)).multiply(new BigDecimal(b));
				if (c == (double)0.0F) {
					return a != (double)0.0F && b != (double)0.0F ? product.doubleValue() : a * b + c;
				} else {
					return product.add(new BigDecimal(c)).doubleValue();
				}
			} else if ((!infiniteA || b != (double)0.0F) && (!infiniteB || a != (double)0.0F)) {
				double product = a * b;
				if (Double.isInfinite(product) && !infiniteA && !infiniteB) {
					assert Double.isInfinite(c);

					return c;
				} else {
					double result = product + c;

					assert !Double.isFinite(result);

					return result;
				}
			} else {
				return Double.NaN;
			}
		} else {
			return Double.NaN;
		}
	}


	public float fma(float a, float b, float c) {
		if (Float.isFinite(a) && Float.isFinite(b) && Float.isFinite(c)) {
			return (double)a != (double)0.0F && (double)b != (double)0.0F ? (BigDecimal.valueOf((double) a * (double) b)).add(BigDecimal.valueOf(c)).floatValue() : a * b + c;
		} else {
			return (float)fma((double)a, (double)b, (double)c);
		}
	}

	public double ulp(double d) {
		int exp = getExponent(d);
		double var10000;
		switch (exp) {
			case -1023:
				var10000 = Double.MIN_VALUE;
				break;
			case 1024:
				var10000 = abs(d);
				break;
			default:
				assert exp <= 1023 && exp >= -1022;

				exp -= 52;
				var10000 = exp >= -1022 ? powerOfTwoD(exp) : Double.longBitsToDouble(1L << exp - -1074);
		}

		return var10000;
	}

	public float ulp(float f) {
		int exp = getExponent(f);
		float var10000;
		switch (exp) {
			case -127:
				var10000 = Float.MIN_VALUE;
				break;
			case 128:
				var10000 = abs(f);
				break;
			default:
				assert exp <= 127 && exp >= -126;

				exp -= 23;
				var10000 = exp >= -126 ? powerOfTwoF(exp) : Float.intBitsToFloat(1 << exp - -149);
		}

		return var10000;
	}


	public double signum(double d) {
		return d != (double)0.0F && !Double.isNaN(d) ? copySign((double)1.0F, d) : d;
	}


	public float signum(float f) {
		return f != 0.0F && !Float.isNaN(f) ? copySign(1.0F, f) : f;
	}

	public double sinh(double x) {
		return StrictMath.sinh(x);
	}

	public double cosh(double x) {
		return StrictMath.cosh(x);
	}

	public double tanh(double x) {
		return StrictMath.tanh(x);
	}

	public double hypot(double x, double y) {
		return StrictMath.hypot(x, y);
	}

	public double expm1(double x) {
		return StrictMath.expm1(x);
	}

	public double log1p(double x) {
		return StrictMath.log1p(x);
	}


	public double copySign(double magnitude, double sign) {
		return Double.longBitsToDouble(Double.doubleToRawLongBits(sign) & Long.MIN_VALUE | Double.doubleToRawLongBits(magnitude) & Long.MAX_VALUE);
	}


	public float copySign(float magnitude, float sign) {
		return Float.intBitsToFloat(Float.floatToRawIntBits(sign) & Integer.MIN_VALUE | Float.floatToRawIntBits(magnitude) & Integer.MAX_VALUE);
	}

	public int getExponent(float f) {
		return ((Float.floatToRawIntBits(f) & 2139095040) >> 23) - 127;
	}

	public int getExponent(double d) {
		return (int)(((Double.doubleToRawLongBits(d) & 9218868437227405312L) >> 52) - 1023L);
	}

	public double nextAfter(double start, double direction) {
		if (start > direction) {
			if (start != (double)0.0F) {
				long transducer = Double.doubleToRawLongBits(start);
				return Double.longBitsToDouble(transducer + (transducer > 0L ? -1L : 1L));
			} else {
				return -Double.MIN_VALUE;
			}
		} else if (start < direction) {
			long transducer = Double.doubleToRawLongBits(start + (double)0.0F);
			return Double.longBitsToDouble(transducer + (transducer >= 0L ? 1L : -1L));
		} else {
			return start == direction ? direction : start + direction;
		}
	}

	public float nextAfter(float start, double direction) {
		if ((double)start > direction) {
			if (start != 0.0F) {
				int transducer = Float.floatToRawIntBits(start);
				return Float.intBitsToFloat(transducer + (transducer > 0 ? -1 : 1));
			} else {
				return -Float.MIN_VALUE;
			}
		} else if ((double)start < direction) {
			int transducer = Float.floatToRawIntBits(start + 0.0F);
			return Float.intBitsToFloat(transducer + (transducer >= 0 ? 1 : -1));
		} else {
			return (double)start == direction ? (float)direction : start + (float)direction;
		}
	}

	public double nextUp(double d) {
		if (d < Double.POSITIVE_INFINITY) {
			long transducer = Double.doubleToRawLongBits(d + (double)0.0F);
			return Double.longBitsToDouble(transducer + (transducer >= 0L ? 1L : -1L));
		} else {
			return d;
		}
	}

	public float nextUp(float f) {
		if (f < Float.POSITIVE_INFINITY) {
			int transducer = Float.floatToRawIntBits(f + 0.0F);
			return Float.intBitsToFloat(transducer + (transducer >= 0 ? 1 : -1));
		} else {
			return f;
		}
	}

	public double nextDown(double d) {
		if (!Double.isNaN(d) && d != Double.NEGATIVE_INFINITY) {
			return d == (double)0.0F ? -Double.MIN_VALUE : Double.longBitsToDouble(Double.doubleToRawLongBits(d) + (d > (double)0.0F ? -1L : 1L));
		} else {
			return d;
		}
	}

	public float nextDown(float f) {
		if (!Float.isNaN(f) && f != Float.NEGATIVE_INFINITY) {
			return f == 0.0F ? -Float.MIN_VALUE : Float.intBitsToFloat(Float.floatToRawIntBits(f) + (f > 0.0F ? -1 : 1));
		} else {
			return f;
		}
	}

	public double scalb(double d, int scaleFactor) {
		int MAX_SCALE = 2099;
		int exp_adjust = 0;
		int scale_increment = 0;
		double exp_delta = Double.NaN;
		if (scaleFactor < 0) {
			scaleFactor = max(scaleFactor, -2099);
			scale_increment = -512;
			exp_delta = twoToTheDoubleScaleDown;
		} else {
			scaleFactor = min(scaleFactor, 2099);
			scale_increment = 512;
			exp_delta = twoToTheDoubleScaleUp;
		}

		int t = scaleFactor >> 8 >>> 23;
		exp_adjust = (scaleFactor + t & 511) - t;
		d *= powerOfTwoD(exp_adjust);

		for(int var11 = scaleFactor - exp_adjust; var11 != 0; var11 -= scale_increment) {
			d *= exp_delta;
		}

		return d;
	}

	public float scalb(float f, int scaleFactor) {
		int MAX_SCALE = 278;
		scaleFactor = max(min(scaleFactor, 278), -278);
		return (float)((double)f * powerOfTwoD(scaleFactor));
	}

	double powerOfTwoD(int n) {
		assert n >= -1022 && n <= 1023;

		return Double.longBitsToDouble((long)n + 1023L << 52 & 9218868437227405312L);
	}

	float powerOfTwoF(int n) {
		assert n >= -126 && n <= 127;

		return Float.intBitsToFloat(n + 127 << 23 & 2139095040);
	}

	private final class RandomNumberGeneratorHolder {
		static final Random randomNumberGenerator = new Random();
	}
}

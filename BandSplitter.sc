BandSplitter : Filter {
	*lowpass {
		|rate, sig, f, order=2|
		if (order == 2) {
			^LPF.perform(rate, LPF.perform(rate, sig, f), f)
		} {
			^BLPF.perform(rate, BLPF.perform(rate, sig, order, f), order, f)
		}
	}

	*highpass {
		|rate, sig, f, order=2|
		if (order == 2) {
			^HPF.perform(rate, HPF.perform(rate, sig, f), f)
		} {
			^BHPF.perform(rate, BHPF.perform(rate, sig, order, f), order, f)
		}

	}

	*allpass {
		|rate, sig, f, order=2|
		^(BandSplitter.highpass(rate, sig, f, order) + BandSplitter.lowpass(rate, sig, f, order))
	}
}

BandSplitter2 : BandSplitter {
	*new1 {
		|rate, sig, freq, order=2|
		var a, b;

		//  0hz freq nyquist
		//  |  a  |  b  |

		a = this.lowpass(rate, sig, freq, order);
		b = this.highpass(rate, sig, freq, order);
		^[a, b];
	}

	*ar {
		|sig, freq, order=2|
		^this.new1(\ar, sig, freq, order);
	}

	*kr {
		|sig, freq, order=2|
		^this.new1(\kr, sig, freq, order);
	}
}

BandSplitter4 : BandSplitter {
	*new1 {
		|rate, sig, f1, f2, f3, order=2|
		var low, high, a, b, c, d;

		//  0hz  f1    f2    f3   nyquist
		//  |  a  |  b  |  c  |  d  |

		#low, high = BandSplitter2.new1(rate, sig, f2, order);

		a = this.lowpass(rate, low, f1, order);
		b = this.highpass(rate, low, f1, order);

		c = this.lowpass(rate, high, f3, order);
		d = this.highpass(rate, high, f3, order);

		// Align the phase of the top half with the phase of the bottom half.
		c = this.allpass(rate, c, f1, order);
		d = this.allpass(rate, d, f1, order);

		a = this.allpass(rate, a, f3, order);
		b = this.allpass(rate, b, f3, order);

		^[a, b, c, d]
	}

	*ar {
		|sig, f1, f2, f3, order=2|
		^this.new1(\ar, sig, f1, f2, f3, order);
	}

	*kr {
		|sig, f1, f2, f3, order=2|
		^this.new1(\kr, sig, f1, f2, f3, order);
	}
}

BandSplitter8 : BandSplitter {
	*new1 {
		|rate, sig, f1, f2, f3, f4, f5, f6, f7, order=4|
		var low, high, a, b, c, d, e, f, g, h;

		//  0hz  f1    f2    f3   nyquist
		//  |  a  |  b  |  c  |  d  |

		#low, high = BandSplitter2.new1(rate, sig, order);
		#a, b, c, d = this.allpass(rate, BandSplitter4.new1(rate, low, f1, f2, f3, order), f6, order);
		#e, f, g, h = this.allpass(rate, BandSplitter4.new1(rate, high, f5, f6, f7, order), f2, order);

		^[a, b, c, d, e, f, g, h]
	}

	*ar {
		|sig, f1, f2, f3, f4, f5, f6, f7, order=4|
		^this.new1(\ar, sig, f1, f2, f3, f4, f5, f6, f7, order);
	}

	*kr {
		|sig, f1, f2, f3, f4, f5, f6, f7, order=4|
		^this.new1(\kr, sig, f1, f2, f3, f4, f5, f6, f7, order);
	}
}
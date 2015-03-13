package ee.telekom.workflow.util.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestObject {

	private Object _null;
	private boolean _boolean;
	private Boolean _Boolean;
	private byte _byte;
	private Byte _Byte;
	private short _short;
	private Short _Short;
	private int _int;
	private Integer _Integer;
	private long _long;
	private Long _Long;
	private float _float;
	private Float _Float;
	private double _double;
	private Double _Double;
	private BigInteger _BigInteger;
	private BigDecimal _BigDecimal;
	private String _String;
	private TestEnum _TestEnum;
	private Date _Date;
	private TestObject _TestObject;

	private TestObject[] _array;
	private List<TestObject> _list;
	private Set<TestObject> _set;
	private Map<String, TestObject> _map;

	public static TestObject createComplex() {
		TestObject result = createSimple();
		result._TestObject = createSimple();
		result._array = new TestObject[]{createSimple(), null, createSimple()};
		result._list = Collections.singletonList(createSimple());
		result._set = Collections.singleton(createSimple());
		result._map = Collections.singletonMap("test", createSimple());
		return result;
	}

	public static TestObject createSimple() {
		return create(false, Byte.MIN_VALUE, Short.MIN_VALUE,
				Integer.MAX_VALUE, Long.MIN_VALUE, Float.MAX_VALUE,
				Double.MAX_VALUE, new BigInteger("9" + Long.MAX_VALUE),
				new BigDecimal(Long.MAX_VALUE).add(BigDecimal.TEN), "8",
				TestEnum.ONE, new Date());
	}

	public static TestObject create(boolean _boolean, byte _byte, short _short,
			int _int, long _long, float _float, double _double,
			BigInteger _BigInteger, BigDecimal _BigDecimal, String _String,
			TestEnum _TestEnum, Date _Date) {
		TestObject result = new TestObject();
		result.set(_boolean, _byte, _short, _int, _long, _float, _double,
				_BigInteger, _BigDecimal, _String, _TestEnum, _Date);
		return result;
	}

	protected void set(boolean _boolean, byte _byte, short _short, int _int,
			long _long, float _float, double _double, BigInteger _BigInteger,
			BigDecimal _BigDecimal, String _String, TestEnum _TestEnum,
			Date _Date) {
		this._boolean = _boolean;
		this._Boolean = _boolean;
		this._byte = _byte;
		this._Byte = _byte;
		this._short = _short;
		this._Short = _short;
		this._int = _int;
		this._Integer = _int;
		this._long = _long;
		this._Long = _long;
		this._float = _float;
		this._Float = _float;
		this._double = _double;
		this._Double = _double;
		this._BigInteger = _BigInteger;
		this._BigDecimal = _BigDecimal;
		this._String = _String;
		this._TestEnum = _TestEnum;
		this._Date = _Date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_BigDecimal == null) ? 0 : _BigDecimal.hashCode());
		result = prime * result
				+ ((_BigInteger == null) ? 0 : _BigInteger.hashCode());
		result = prime * result
				+ ((_Boolean == null) ? 0 : _Boolean.hashCode());
		result = prime * result + ((_Byte == null) ? 0 : _Byte.hashCode());
		result = prime * result + ((_Date == null) ? 0 : _Date.hashCode());
		result = prime * result + ((_Double == null) ? 0 : _Double.hashCode());
		result = prime * result + ((_Float == null) ? 0 : _Float.hashCode());
		result = prime * result
				+ ((_Integer == null) ? 0 : _Integer.hashCode());
		result = prime * result + ((_Long == null) ? 0 : _Long.hashCode());
		result = prime * result + ((_Short == null) ? 0 : _Short.hashCode());
		result = prime * result + ((_String == null) ? 0 : _String.hashCode());
		result = prime * result
				+ ((_TestEnum == null) ? 0 : _TestEnum.hashCode());
		result = prime * result
				+ ((_TestObject == null) ? 0 : _TestObject.hashCode());
		result = prime * result + Arrays.hashCode(_array);
		result = prime * result + (_boolean ? 1231 : 1237);
		result = prime * result + _byte;
		long temp;
		temp = Double.doubleToLongBits(_double);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Float.floatToIntBits(_float);
		result = prime * result + _int;
		result = prime * result + ((_list == null) ? 0 : _list.hashCode());
		result = prime * result + (int) (_long ^ (_long >>> 32));
		result = prime * result + ((_map == null) ? 0 : _map.hashCode());
		result = prime * result + ((_null == null) ? 0 : _null.hashCode());
		result = prime * result + ((_set == null) ? 0 : _set.hashCode());
		result = prime * result + _short;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TestObject)) {
			return false;
		}
		TestObject other = (TestObject) obj;
		if (_BigDecimal == null) {
			if (other._BigDecimal != null) {
				return false;
			}
		} else if (!_BigDecimal.equals(other._BigDecimal)) {
			return false;
		}
		if (_BigInteger == null) {
			if (other._BigInteger != null) {
				return false;
			}
		} else if (!_BigInteger.equals(other._BigInteger)) {
			return false;
		}
		if (_Boolean == null) {
			if (other._Boolean != null) {
				return false;
			}
		} else if (!_Boolean.equals(other._Boolean)) {
			return false;
		}
		if (_Byte == null) {
			if (other._Byte != null) {
				return false;
			}
		} else if (!_Byte.equals(other._Byte)) {
			return false;
		}
		if (_Date == null) {
			if (other._Date != null) {
				return false;
			}
		} else if (!_Date.equals(other._Date)) {
			return false;
		}
		if (_Double == null) {
			if (other._Double != null) {
				return false;
			}
		} else if (!_Double.equals(other._Double)) {
			return false;
		}
		if (_Float == null) {
			if (other._Float != null) {
				return false;
			}
		} else if (!_Float.equals(other._Float)) {
			return false;
		}
		if (_Integer == null) {
			if (other._Integer != null) {
				return false;
			}
		} else if (!_Integer.equals(other._Integer)) {
			return false;
		}
		if (_Long == null) {
			if (other._Long != null) {
				return false;
			}
		} else if (!_Long.equals(other._Long)) {
			return false;
		}
		if (_Short == null) {
			if (other._Short != null) {
				return false;
			}
		} else if (!_Short.equals(other._Short)) {
			return false;
		}
		if (_String == null) {
			if (other._String != null) {
				return false;
			}
		} else if (!_String.equals(other._String)) {
			return false;
		}
		if (_TestEnum != other._TestEnum) {
			return false;
		}
		if (_TestObject == null) {
			if (other._TestObject != null) {
				return false;
			}
		} else if (!_TestObject.equals(other._TestObject)) {
			return false;
		}
		if (!Arrays.equals(_array, other._array)) {
			return false;
		}
		if (_boolean != other._boolean) {
			return false;
		}
		if (_byte != other._byte) {
			return false;
		}
		if (Double.doubleToLongBits(_double) != Double
				.doubleToLongBits(other._double)) {
			return false;
		}
		if (Float.floatToIntBits(_float) != Float.floatToIntBits(other._float)) {
			return false;
		}
		if (_int != other._int) {
			return false;
		}
		if (_list == null) {
			if (other._list != null) {
				return false;
			}
		} else if (!_list.equals(other._list)) {
			return false;
		}
		if (_long != other._long) {
			return false;
		}
		if (_map == null) {
			if (other._map != null) {
				return false;
			}
		} else if (!_map.equals(other._map)) {
			return false;
		}
		if (_null == null) {
			if (other._null != null) {
				return false;
			}
		} else if (!_null.equals(other._null)) {
			return false;
		}
		if (_set == null) {
			if (other._set != null) {
				return false;
			}
		} else if (!_set.equals(other._set)) {
			return false;
		}
		if (_short != other._short) {
			return false;
		}
		return true;
	}

}

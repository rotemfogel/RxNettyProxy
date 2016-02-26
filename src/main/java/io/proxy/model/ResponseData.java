package io.proxy.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

import static io.proxy.utils.StringUtils.isEmpty;

public class ResponseData {
	private final static Gson	gson	= new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	@Expose
	private float				p		= -1;
	@Expose
	private String				c;
	@Expose
	private String				o;
	@Expose
	private String				a;
	@Expose
	private String				i;
	@Expose
	private String				t;
	@Expose
	private String				x;
	@Expose
	private String				u;

	public static ResponseData parse(final String json) {
		if (!isEmpty(json)) {
			try {
				return gson.fromJson(json, ResponseData.class);
			} catch (JsonSyntaxException ignored) {
			}
		}
		return null;
	}

	public boolean hasP() {
		return p != -1;
	}

	public float getP() {
		return p;
	}

	public String getC() {
		return c;
	}

	public String getO() {
		return o;
	}

	public String getA() {
		return a;
	}

	public String getI() {
		return i;
	}

	public String getT() {
		return t;
	}

	public String getX() {
		return x;
	}

	public String getU() {
		return u;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ResponseData other = (ResponseData) obj;
		if (a == null) {
			if (other.a != null) return false;
		}
		else if (!a.equals(other.a)) return false;
		if (c == null) {
			if (other.c != null) return false;
		}
		else if (!c.equals(other.c)) return false;
		if (i == null) {
			if (other.i != null) return false;
		}
		else if (!i.equals(other.i)) return false;
		if (o == null) {
			if (other.o != null) return false;
		}
		else if (!o.equals(other.o)) return false;
		if (Float.floatToIntBits(p) != Float.floatToIntBits(other.p)) return false;
		if (t == null) {
			if (other.t != null) return false;
		}
		else if (!t.equals(other.t)) return false;
		if (u == null) {
			if (other.u != null) return false;
		}
		else if (!u.equals(other.u)) return false;
		if (x == null) {
			if (other.x != null) return false;
		}
		else if (!x.equals(other.x)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		result = prime * result + ((i == null) ? 0 : i.hashCode());
		result = prime * result + ((o == null) ? 0 : o.hashCode());
		result = prime * result + Float.floatToIntBits(p);
		result = prime * result + ((t == null) ? 0 : t.hashCode());
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return gson.toJson(this);
	}
}

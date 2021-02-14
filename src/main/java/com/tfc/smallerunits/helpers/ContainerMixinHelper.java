package com.tfc.smallerunits.helpers;

import com.tfc.smallerunits.mixins.ContainerAccessor;
import net.minecraft.inventory.container.Container;

import java.lang.reflect.Method;

public class ContainerMixinHelper {
	private static final Method setClosable;
	private static final Method getCanCloseNaturally;
	
	static {
		Method m0;
		Method m1;
		try {
			m0 = Container.class.getMethod("setCanCloseNaturally", boolean.class);
			m1 = Container.class.getMethod("canCloseNaturally");
		} catch (Throwable err) {
			try {
				m0 = Container.class.getDeclaredMethod("setCanCloseNaturally", boolean.class);
				m1 = Container.class.getDeclaredMethod("canCloseNaturally");
			} catch (Throwable err1) {
				try {
					m0 = ContainerAccessor.class.getMethod("setCanCloseNaturally", boolean.class);
					m1 = ContainerAccessor.class.getMethod("canCloseNaturally");
				} catch (Throwable err2) {
					try {
						m0 = ContainerAccessor.class.getDeclaredMethod("setCanCloseNaturally", boolean.class);
						m1 = ContainerAccessor.class.getDeclaredMethod("canCloseNaturally");
					} catch (Throwable err3) {
						throw new RuntimeException(err);
					}
				}
			}
		}
		setClosable = m0;
		getCanCloseNaturally = m1;
	}
	
	public static void setNaturallyClosable(Container container, boolean naturallyClosable) {
		if (container instanceof ContainerAccessor) {
			((ContainerAccessor) container).setCanCloseNaturally(naturallyClosable);
		} else {
			try {
				setClosable.invoke(container, naturallyClosable);
			} catch (Throwable ignored) {
			}
		}
	}
	
	public static boolean getNaturallyClosable(Container container) {
		if (container instanceof ContainerAccessor) {
			return ((ContainerAccessor) container).canCloseNaturally();
		} else {
			try {
				return (boolean) getCanCloseNaturally.invoke(container);
			} catch (Throwable ignored) {
				return true;
			}
		}
	}
}

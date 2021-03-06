package ch.quantasy.tinkerforge.tinker.core.implementation;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import com.tinkerforge.IPConnection.ConnectedListener;
import com.tinkerforge.IPConnection.DisconnectedListener;
import com.tinkerforge.IPConnection.EnumerateListener;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TinkerforgeException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class tries to give a helping hand in order to represent a Tinkerforge-stack. It gives convenience-methods for
 * the Java-Developer.
 *
 * @author reto
 *
 */
public abstract class AbstractTinkerforgeStackManager {

    private final TinkerforgeStackAddress stackAddress;

    private final IPConnection ipConnection;
    private final Map<String, Device> deviceMap;

    private final IPConnectionHandler ipConnectionHandler;
    private final DeviceEnumerationHandler deviceEnumerationHandler;

    /**
     * Creates a representation of a Tinkerforge-Stack. Either at 'localhost' or via a 'Server'-name given in the
     * WLAN-Brick
     *
     * @param stackAddress
     */
    public AbstractTinkerforgeStackManager(final TinkerforgeStackAddress stackAddress) {
	if (stackAddress == null) {
	    throw new IllegalArgumentException();
	}
	this.deviceMap = new HashMap<String, Device>();
	this.stackAddress = stackAddress;
	this.ipConnection = new IPConnection();
	this.ipConnectionHandler = new IPConnectionHandler(this.ipConnection);
	this.deviceEnumerationHandler = new DeviceEnumerationHandler(this.ipConnection);
    }

    public TinkerforgeStackAddress getStackAddress() {
	return stackAddress;
    }

    /**
     * Connects to the real Tinkerforge-Stack. It then requests the connected {@link Device}s. Therefore it waits for
     * some time (3-seconds)
     *
     * @throws UnknownHostException
     * @throws IOException
     */
    protected void connect() throws UnknownHostException, IOException {
	try {
	    this.ipConnection.connect(stackAddress.hostName, stackAddress.port);
	    Thread.sleep(3000);
	} catch (final AlreadyConnectedException e) {
	    // So what
	} catch (final InterruptedException e) {
	    // OK, we go on
	}
    }

    /**
     * Disconnects from a real Tinkerforge-Stack.
     */
    protected void disconnect() {
	try {
	    this.ipConnection.disconnect();
	} catch (final NotConnectedException e) {
	    // So what
	}
    }

    public boolean isConnected() {
	return (this.ipConnection != null)
		&& (this.ipConnection.getConnectionState() == IPConnection.CONNECTION_STATE_CONNECTED);
    }

    /**
     * Called when the brick has been connected
     */
    protected abstract void connected();

    /**
     * Called when the Brick has been disconnected
     */
    protected abstract void disconnected();

    /**
     * Called when a new {@link Device} has been found/connected
     *
     * @param device
     */
    protected abstract void deviceConnected(Device device);

    /**
     * Called when a {@link Device} already known to the system has been reconnected. A good strategy is to drop the old
     * device-instance and connect the Listeners to this new instance of the {@link Device}.
     *
     * @param device
     */
    protected abstract void deviceReConnected(Device device);

    /**
     * Called when a {@link Device} already known to the system has been disconnected.
     *
     * @param device
     */
    protected abstract void deviceDisconnected(Device device);

    public boolean isConnected(final Device device) {
	if (device == null) {
	    throw new IllegalArgumentException();
	}
	try {
	    device.getIdentity();
	    return true;
	} catch (final TinkerforgeException e) {
	    e.printStackTrace();
	}
	return false;
    }

    /**
     * Returns the most recent list of all valid connected {@link Device}s.
     *
     * @return
     */
    public List<Device> getConnectedDeviceList() {
	return new ArrayList<Device>(this.deviceMap.values());
    }

    private class IPConnectionHandler implements ConnectedListener, DisconnectedListener {

	private final IPConnection connection;

	public IPConnectionHandler(final IPConnection connection) {
	    this.connection = connection;
	    connection.addConnectedListener(this);
	    connection.addDisconnectedListener(this);
	}

	@Override
	public void disconnected(final short disconnectReason) {
	    AbstractTinkerforgeStackManager.this.disconnected();

	}

	@Override
	public void connected(final short disconnectReason) {
	    try {
		this.connection.enumerate();
		AbstractTinkerforgeStackManager.this.connected();
	    } catch (final Exception ex) {
		// Well, this should not happen?!
		// But will treat it gracefully.
		ex.printStackTrace();
	    }
	}
    }

    private class DeviceEnumerationHandler implements EnumerateListener {

	private final IPConnection connection;

	public DeviceEnumerationHandler(final IPConnection connection) {
	    this.connection = connection;
	    this.connection.addEnumerateListener(this);
	}

	@Override
	public void enumerate(final String uid, final String connectedUid, final char position,
		final short[] hardwareVersion, final short[] firmwareVersion, final int deviceIdentifier,
		final short enumerationType) {
	    boolean isNewDevice = this.createDevice(deviceIdentifier, uid);
	    switch (enumerationType) {
		case IPConnection.ENUMERATION_TYPE_AVAILABLE:
		    if (isNewDevice) {
			AbstractTinkerforgeStackManager.this.deviceConnected(AbstractTinkerforgeStackManager.this.deviceMap.get(uid));
		    }
		    break;
		case IPConnection.ENUMERATION_TYPE_CONNECTED:
		    System.out.println("Reconnect");
		    if (isNewDevice) {
			AbstractTinkerforgeStackManager.this.deviceConnected(AbstractTinkerforgeStackManager.this.deviceMap.get(uid));
		    } else {
			AbstractTinkerforgeStackManager.this.deviceReConnected(AbstractTinkerforgeStackManager.this.deviceMap.get(uid));
		    }
		    break;
		case IPConnection.ENUMERATION_TYPE_DISCONNECTED:
		    if (isNewDevice) {
			// That is strange!
			deviceMap.remove(uid);
			return;
		    }
		    AbstractTinkerforgeStackManager.this
			    .deviceDisconnected(AbstractTinkerforgeStackManager.this.deviceMap.get(uid));
		    break;
		default:
		    System.out.println("!!!Unknown cause: " + enumerationType);
	    }

	}

	private boolean createDevice(final int deviceIdentifier, final String uid) {
	    if (AbstractTinkerforgeStackManager.this.deviceMap.containsKey(uid)) {
		return false;
	    }
	    try {
		TinkerforgeDevice tinkerforgeDevice = TinkerforgeDevice.getDevice(deviceIdentifier);
		if (tinkerforgeDevice == null) {
		    return false;
		}
		final Device device = (Device) tinkerforgeDevice.deviceClass
			.getDeclaredConstructor(String.class, IPConnection.class).newInstance(uid,
											      AbstractTinkerforgeStackManager.this.ipConnection);
		device.getIdentity();
		AbstractTinkerforgeStackManager.this.deviceMap.put(uid, device);
		return true;
	    } catch (final TinkerforgeException ex) {
		ex.printStackTrace();
	    } catch (final Exception ex) {
		ex.printStackTrace();
	    }
	    return false;
	}

    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 37 * hash + Objects.hashCode(this.stackAddress);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final AbstractTinkerforgeStackManager other = (AbstractTinkerforgeStackManager) obj;
	if (!Objects.equals(this.stackAddress, other.stackAddress)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	return getClass().getName() + "{" + "stackAddress=" + stackAddress + '}';
    }

}

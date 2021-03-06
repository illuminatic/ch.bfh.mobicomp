/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.quantasy.tinkerbus.service.device.deviceAmbientLight;

import ch.quantasy.messagebus.message.definition.Content;
import ch.quantasy.messagebus.worker.definition.Agent;
import ch.quantasy.messagebus.worker.definition.Service;
import ch.quantasy.tinkerbus.service.device.content.TinkerforgeDeviceContent;
import ch.quantasy.tinkerbus.service.device.core.ATinkerforgeDeviceService;
import ch.quantasy.tinkerbus.service.device.deviceAmbientLight.content.AnalogValueCallbackPeriodContent;
import ch.quantasy.tinkerbus.service.device.deviceAmbientLight.content.AnalogValueContent;
import ch.quantasy.tinkerbus.service.device.deviceAmbientLight.content.AnalogValueThresholdContent;
import ch.quantasy.tinkerbus.service.device.deviceAmbientLight.content.DebouncePeriodContent;
import ch.quantasy.tinkerbus.service.device.deviceAmbientLight.content.IlluminanceCallbackPeriodContent;
import ch.quantasy.tinkerbus.service.device.deviceAmbientLight.content.IlluminanceThresholdContent;
import ch.quantasy.tinkerbus.service.device.deviceAmbientLight.content.IlluminanceValueContent;
import ch.quantasy.tinkerbus.service.device.deviceAmbientLight.message.TinkerforgeAmbientLightEvent;
import ch.quantasy.tinkerbus.service.device.deviceAmbientLight.message.TinkerforgeAmbientLightIntent;
import ch.quantasy.tinkerbus.service.device.message.ATinkerforgeDeviceEvent;
import ch.quantasy.tinkerbus.service.device.message.ATinkerforgeDeviceIntent;
import ch.quantasy.tinkerbus.service.device.threshold.CallbackThreshold;
import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class TinkerforgeAmbientLightService extends ATinkerforgeDeviceService<BrickletAmbientLight, TinkerforgeAmbientLightIntent, TinkerforgeAmbientLightEvent> implements BrickletAmbientLight.AnalogValueListener, BrickletAmbientLight.AnalogValueReachedListener, BrickletAmbientLight.IlluminanceListener, BrickletAmbientLight.IlluminanceReachedListener {

    public TinkerforgeAmbientLightService(BrickletAmbientLight device, String deviceID) {
	super(device, deviceID);
    }

    @Override
    protected void updateListeners(BrickletAmbientLight device) {
	if (device == null) {
	    return;
	}
	device.addAnalogValueListener(this);
	device.addAnalogValueReachedListener(this);
	device.addIlluminanceListener(this);
	device.addIlluminanceReachedListener(this);
    }

    @Override
    protected void updateSettingsOnDevice(BrickletAmbientLight device, Map<Class, Content> settings) {
	if (settings == null || settings.isEmpty() || device == null) {
	    return;
	}
	try {
	    {
		AnalogValueCallbackPeriodContent content = ((AnalogValueCallbackPeriodContent) (settings.get(AnalogValueCallbackPeriodContent.class)));
		if (content != null) {
		    Long value = content.getValue();
		    if (value != null) {
			device.setAnalogValueCallbackPeriod(value);
		    }
		}
	    }
	    {
		AnalogValueThresholdContent content = ((AnalogValueThresholdContent) (settings.get(AnalogValueThresholdContent.class)));
		if (content != null) {
		    CallbackThreshold value = content.getValue();
		    if (value != null) {
			device.setAnalogValueCallbackThreshold(value.option, value.min, value.max);
		    }
		}
	    }
	    {

		DebouncePeriodContent content = ((DebouncePeriodContent) (settings.get(DebouncePeriodContent.class)));
		if (content != null) {
		    Long value = content.getValue();
		    if (value != null) {
			device.setDebouncePeriod(value);
		    }
		}
	    }
	    {
		IlluminanceThresholdContent content = ((IlluminanceThresholdContent) (settings.get(IlluminanceThresholdContent.class)));
		if (content != null) {
		    CallbackThreshold value = content.getValue();
		    if (value != null) {
			device.setIlluminanceCallbackThreshold(value.option, value.min, value.max);
		    }
		}
	    }
	    {
		IlluminanceCallbackPeriodContent content = ((IlluminanceCallbackPeriodContent) (settings.get(IlluminanceCallbackPeriodContent.class)));
		if (content != null) {
		    Long value = content.getValue();

		    if (value != null) {
			device.setIlluminanceCallbackPeriod(value);
		    }
		}
	    }

	} catch (TimeoutException ex) {
	    Logger.getLogger(TinkerforgeAmbientLightService.class.getName()).log(Level.SEVERE, null, ex);
	} catch (NotConnectedException ex) {
	    Logger.getLogger(TinkerforgeAmbientLightService.class.getName()).log(Level.SEVERE, null, ex);
	}

    }

    @Override
    public void handleTinkerforgeIntent(TinkerforgeAmbientLightIntent message) {
	//Nothing special
    }

    @Override
    public void analogValue(int value
    ) {
	getDeviceContent().updateEmission(new AnalogValueContent(value));
	TinkerforgeAmbientLightEvent event = createEvent();
	publish(event);
    }

    @Override
    public void analogValueReached(int value
    ) {
	getDeviceContent().updateEmission(new AnalogValueContent(value));
	TinkerforgeAmbientLightEvent event = createEvent();
	publish(event);
    }

    @Override
    public void illuminance(int illuminance
    ) {
	getDeviceContent().updateEmission(new IlluminanceValueContent(illuminance));
	TinkerforgeAmbientLightEvent event = createEvent();
	publish(event);
    }

    @Override
    public void illuminanceReached(int illuminance
    ) {
	getDeviceContent().updateEmission(new IlluminanceValueContent(illuminance));
	TinkerforgeAmbientLightEvent event = createEvent();
	publish(event);
    }

    @Override
    public TinkerforgeAmbientLightEvent createEvent() {
	return new Event(getDeviceContent(), this);
    }

    public static TinkerforgeAmbientLightIntent createIntent(TinkerforgeDeviceContent content, Agent agent) {
	return new Intent(content, agent);
    }

}

class Intent extends ATinkerforgeDeviceIntent implements TinkerforgeAmbientLightIntent {

    public Intent(TinkerforgeDeviceContent deviceContent, Agent intentSender) {
	super(deviceContent, intentSender);
    }

    public Intent(TinkerforgeDeviceContent deviceContent, Agent intentSender, String... intentReceivers) {
	super(deviceContent, intentSender, intentReceivers);
    }

}

class Event extends ATinkerforgeDeviceEvent implements TinkerforgeAmbientLightEvent {

    public Event(TinkerforgeDeviceContent deviceContent, Service eventSender) {
	super(deviceContent, eventSender);
    }

}

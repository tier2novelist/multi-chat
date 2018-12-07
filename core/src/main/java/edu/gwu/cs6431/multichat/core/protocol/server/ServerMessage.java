package edu.gwu.cs6431.multichat.core.protocol.server;

import edu.gwu.cs6431.multichat.core.protocol.client.ClientMessage;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public interface ServerMessage {
    /**
     * Copy inherited fields from client message
     * @param clientMessage client message
     * @return server message with inherited fields
     */
    default ServerMessage from(ClientMessage clientMessage) {
        Field[] clientMessageFields = ClientMessage.class.getDeclaredFields();
        Map<String, Object> fieldNameToValue = new HashMap<>();
        for(Field clientMessageField : clientMessageFields) {
            if(clientMessageField.isAnnotationPresent(edu.gwu.cs6431.multichat.core.protocol.client.HeaderField.class)) {
                edu.gwu.cs6431.multichat.core.protocol.client.HeaderField clientHeaderField = clientMessageField.getAnnotation(edu.gwu.cs6431.multichat.core.protocol.client.HeaderField.class);
                try {
                    PropertyDescriptor prop = new PropertyDescriptor(clientMessageField.getName(), ClientMessage.class);
                    fieldNameToValue.put(clientHeaderField.name(), prop.getReadMethod().invoke(clientMessage));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field : fields) {
            if (field.isAnnotationPresent(HeaderField.class)) {
                HeaderField headerField = field.getAnnotation(HeaderField.class);
                if(headerField.inherited()) {
                    try {
                        PropertyDescriptor prop = new PropertyDescriptor(field.getName(), this.getClass());
                        prop.getWriteMethod().invoke(this, fieldNameToValue.get(headerField.name()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return this;
    }
}

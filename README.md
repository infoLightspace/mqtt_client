This is a sample implementation of the MQTT Client with android.

Requirements:

    You need to have deployed an MQTT Broker. For the tests, EMQX Broker has been used for the demo. You can find the instructions on how to set up a simple working MQTT Broker from: https://docs.emqx.io

    You need these imports in your gradle file:

repositories { maven { url "https://repo.eclipse.org/content/repositories/paho-snapshots/" } }

dependencies { compile 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.0.2' compile 'org.eclipse.paho:org.eclipse.paho.android.service:1.0.2' }

You can find more information at: https://www.eclipse.org/paho/clients/android/

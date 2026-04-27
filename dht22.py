import time
import board
import adafruit_dht

dhtDevice = adafruit_dht.DHT22(board.D18)

while True:
    try:
        temp = dhtDevice.temperature
        humidity = dhtDevice.humidity
        if temp is not None and humidity is not None:
            print(f"{temp:.1f},{humidity:.1f}", flush=True)
    except RuntimeError:
        pass  
    except Exception as error:
        dhtDevice.exit()
        raise error
    time.sleep(2.0)

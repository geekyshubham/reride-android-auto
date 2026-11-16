"""Original deterministic score; no downloaded music or voice provider."""
from pathlib import Path
import math, struct, wave, json
base=Path(__file__).parent/'brag-output/composition/assets';base.mkdir(exist_ok=True)
rate=22050;duration=24;beat=60/110
chords=[(130.8128,164.8138,195.9977,261.6256),(110,130.8128,164.8138,220),(174.6141,220,261.6256,349.2282),(195.9977,246.9417,293.6648,391.9954)]
values=[]
for i in range(rate*duration):
 t=i/rate;chord=chords[min(int(t/6),3)];local=t%6
 pad=sum(math.sin(2*math.pi*f*t)*(.68+.32*math.sin(math.pi*min(local,1)/2)) for f in chord)/4
 phase=t%beat;bass=math.sin(2*math.pi*chord[0]/2*t)*math.exp(-phase*5)
 kick=math.sin(2*math.pi*(48*phase+18*(1-math.exp(-phase*20))))*math.exp(-phase*22)
 pluck=math.sin(2*math.pi*chord[(int(t/beat))%4]*2*t)*math.exp(-phase*10)
 fade=min(1,t/.6,(duration-t)/1.4);values.append((pad*.26+bass*.24+kick*.19+pluck*.08)*fade)
peak=max(abs(x) for x in values);values=[x/peak*.82 for x in values]
with wave.open(str(base/'ride-score.wav'),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes(b''.join(struct.pack('<h',int(x*32767)) for x in values))
energy=[math.sqrt(sum(v*v for v in values[int(n*rate/30):int((n+1)*rate/30)])/int(rate/30)) for n in range(duration*30)]
(base/'energy.js').write_text('window.RIDE_ENERGY='+json.dumps(energy)+';\n')

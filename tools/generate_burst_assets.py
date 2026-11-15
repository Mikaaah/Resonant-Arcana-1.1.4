"""Generate only the five original burst icons and their resources; preserve baseline assets."""
from pathlib import Path
import json, math, zipfile
from PIL import Image, ImageDraw
ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
CAT = json.loads((RES/'assets/resonant_arcana/catalogue.json').read_text())
IDS = ['planet_befall','preserver_of_fortune','musou_shinsetsu','winds_grand_ode','celestial_shower']
COLORS = ['#e5b348','#c3f3ff','#ba89ff','#64e5bd','#8ad8ff']

def write(path, data):
 p=RES/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
with zipfile.ZipFile(ROOT/'baseline/resonant-arcana-1.1.3.jar') as baseline:
 lang=json.loads(baseline.read('assets/resonant_arcana/lang/en_us.json'))
 translations=json.loads((ROOT/'config-translations.json').read_text())
 for index,(id,color) in enumerate(zip(IDS,COLORS)):
  spell=CAT['spells'][id.upper()]
  lang[f'spell.resonant_arcana.{id}']=spell['title'];lang[f'spell.resonant_arcana.{id}.description']=spell['description']
  translations[f'resonant_arcana.visibility.{id}']=spell['title']
  image=Image.new('RGBA',(32,32));d=ImageDraw.Draw(image)
  d.rounded_rectangle((0,0,31,31),radius=4,fill='#14172c',outline='#465374',width=1)
  if index==0:
   d.polygon([(8,10),(17,6),(25,12),(22,22),(13,26),(6,19)],fill='#69513a',outline=color)
   d.line([(8,10),(15,15),(25,12)],fill=color,width=2);d.line([(15,15),(13,26)],fill=color,width=2)
   d.line([(5,4),(9,9)],fill='#fff2ba',width=2);d.line([(4,12),(7,14)],fill=color,width=1)
  elif index==1:
   d.polygon([(9,5),(22,5),(24,24),(17,28),(8,23)],fill='#65778d',outline=color)
   d.line([(16,8),(16,22)],fill='#ffeab4',width=2)
   d.line([(11,12),(21,12)],fill='#ffeab4',width=2);d.line([(12,18),(21,18)],fill=color,width=2)
   d.line([(5,9),(5,15)],fill=color,width=1);d.line([(2,12),(8,12)],fill=color,width=1)
  elif index==2:
   d.polygon([(24,4),(22,15),(10,24),(7,20)],fill=color,outline='#f2deff')
   d.line([(5,19),(12,26)],fill='#7953c6',width=3);d.line([(6,26),(10,22)],fill='#f2deff',width=2)
   d.line([(6,6),(13,9),(9,13),(16,15)],fill=color,width=2)
  elif index==3:
   points=[]
   for i in range(100):
    t=i/99*math.pi*4;r=1+i/99*11;points.append((16+math.cos(t)*r,16+math.sin(t)*r))
   d.line(points,fill=color,width=2);d.line([(4,7),(10,4),(16,5)],fill='#d7fff2',width=2)
  else:
   d.ellipse((10,3,22,15),fill='#397fa6',outline=color,width=2)
   for x,y in [(7,18),(16,23),(25,18)]:
    d.polygon([(x,y-4),(x+2,y),(x,y+5),(x-2,y)],fill=color)
   d.line([(9,10),(4,14)],fill='#d7f4ff',width=1);d.line([(23,10),(27,14)],fill='#d7f4ff',width=1)
  p=RES/f'assets/resonant_arcana/textures/gui/spell_icons/{id}.png';p.parent.mkdir(parents=True,exist_ok=True);image.save(p)
  ingredients=[{'item':'minecraft:paper'},{'item':'irons_spellbooks:legendary_ink'},{'item':'minecraft:nether_star'}]
  materials=[['gold_block','amethyst_shard','obsidian'],['ghast_tear','blue_ice','golden_apple'],['lightning_rod','amethyst_shard','diamond_sword'],['breeze_rod','ender_pearl','feather'],['blue_ice','prismarine_crystals','snow_block']][index]
  ingredients += [{'item':'minecraft:'+material} for material in materials]
  write(f'data/resonant_arcana/recipe/scrolls/{id}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':ingredients,'result':{'id':'irons_spellbooks:scroll','count':1,'components':{'irons_spellbooks:spell_container':{'maxSpells':1,'spellWheel':False,'mustEquip':False,'data':[{'id':'resonant_arcana:'+id,'index':0,'level':1}]}}}})
 for effect,title,index in [('petrified','Petrified',0),('fortune_talisman','Fortune Talisman',1)]:
  lang['effect.resonant_arcana.'+effect]=title
  p=RES/f'assets/resonant_arcana/textures/mob_effect/{effect}.png';p.parent.mkdir(parents=True,exist_ok=True)
  Image.open(RES/f'assets/resonant_arcana/textures/gui/spell_icons/{IDS[index]}.png').resize((18,18),Image.Resampling.NEAREST).save(p)
 write('assets/resonant_arcana/lang/en_us.json',lang)
 (ROOT/'config-translations.json').write_text(json.dumps(translations,indent=2)+'\n')
 schools=['magic','ice','thunder','ender','ice']
 for tag in ['all','tiers/cataclysm','categories/cataclysm',*[f'schools/{x}' for x in set(schools)]]:
  path=f'data/resonant_arcana/tags/irons_spellbooks/spells/{tag}.json';data=json.loads(baseline.read(path))
  added=[id for id,school in zip(IDS,schools) if not tag.startswith('schools/') or tag=='schools/'+school]
  for id in added:
   value='resonant_arcana:'+id
   if value not in data['values']:data['values'].append(value)
  write(path,data)
print('Generated five icons, two status icons, five scroll recipes, search tags and English text.')

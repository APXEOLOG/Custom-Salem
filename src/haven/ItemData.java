package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import haven.Glob.Pagina;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ItemData {
    private static Gson gson;
    private static Map<String, ItemData> item_data = new LinkedHashMap<String, ItemData>(9, 0.75f, true) {
	private static final long serialVersionUID = 1L;

	protected boolean removeEldestEntry(Map.Entry<String, ItemData> eldest) {
	    return size() > 75;
	}

    };

    public FoodInfo.Data food;
    public Inspiration.Data inspiration;
    public GobbleInfo.Data gobble;
    public ArtificeData artifice;
    public int uses;

    public Tex longtip(Resource res) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt = ad.name;
	List<BufferedImage> list = new LinkedList<BufferedImage>();
	if(pg != null){tt += "\n\n" + pg.text;}

	ITipData[] data = new ITipData[]{food, gobble, inspiration, artifice};
	for(ITipData tip : data) {
	    if (tip != null) {
		list.add(tip.create().longtip());
	    }
	}
	if(uses > 0){
	    list.add(RichText.stdf.render(String.format("$b{$col[192,192,64]{Uses: %d}}\n", uses)).img);
	}
	
	BufferedImage img = MenuGrid.ttfnd.render(tt, 300).img;
	for(BufferedImage tmp : list){
	    if(tmp != null){
		img = ItemInfo.catimgs(3, img, tmp);
	    }
	}
	return new TexI(img);
    }
    
    public static interface ITipData {
	ItemInfo.Tip create();
    }
    
    public static void actualize(GItem item, Pagina pagina) {
	String name = item.name();
	if(name == null){ return; }
	
	List<ItemInfo> info = item.info();
	double mult = getMultiplier(info);
	ItemData data = new ItemData();
	data.uses = getUses(info);
	for(ItemInfo ii : info){
	    String className = ii.getClass().getCanonicalName();
	    if(ii instanceof FoodInfo){
		data.food = new FoodInfo.Data((FoodInfo) ii, mult);
	    } else if(ii instanceof Inspiration){
		data.inspiration = new Inspiration.Data((Inspiration) ii);
	    } else if(ii instanceof GobbleInfo){
		data.gobble = new GobbleInfo.Data((GobbleInfo) ii, mult);
	    } else if(className.equals("Slotted")){
		data.artifice = new ArtificeData(ii);
	    }
	}
	name = pagina.res().name;
	item_data.put(name, data);
	store(name, data);
    }

    private static int getUses(List<ItemInfo> info) {
	GItem.NumberInfo ninf = ItemInfo.find(GItem.NumberInfo.class, info);
	if(ninf != null){
	    return ninf.itemnum();
	}
	return -1;
    }

    private static double getMultiplier(List<ItemInfo> info) {
	Alchemy alch = ItemInfo.find(Alchemy.class, info);
	if(alch != null){
	    return 1+alch.purity();
	}
	return 1;
    }

    private static void store(String name, ItemData data) {
	File file = Config.getFile(getFilename(name));
	boolean exists = file.exists();
	if(!exists){
	    try {
		//noinspection ResultOfMethodCallIgnored
		new File(file.getParent()).mkdirs();
		exists = file.createNewFile();
	    } catch (IOException ignored) {}
	}
	if(exists && file.canWrite()){
	    PrintWriter out = null;
	    try {
		out = new PrintWriter(file);
		out.print(getGson().toJson(data));
	    } catch (FileNotFoundException ignored) {
	    } finally {
		if (out != null) {
		    out.close();
		}
	    }
	}
    }

    public static ItemData get(String name) {
	if(item_data.containsKey(name)){
	    return item_data.get(name);
	}
	return load(name);
    }

    private static ItemData load(String name) {
	ItemData data = null;
	String filename = getFilename(name);
	InputStream inputStream = null;
	File file = Config.getFile(filename);
	if(file.exists() && file.canRead()) {
	    try {
		inputStream = new FileInputStream(file);
	    } catch (FileNotFoundException ignored) {
	    }
	} else {
	    inputStream = ItemData.class.getResourceAsStream(filename);
	}
	if(inputStream != null) {
	    data = parseStream(inputStream);
	    item_data.put(name, data);
	}
	return data;
    }

    private static String getFilename(String name) {
	return "/item_data/" + name + ".json";
    }

    private static ItemData parseStream(InputStream inputStream) {
	ItemData data = null;
	try {
	    String json = Utils.stream2str(inputStream);
	    data =  getGson().fromJson(json, ItemData.class);
	} catch (JsonSyntaxException ignore){
	} finally {
	    try {inputStream.close();} catch (IOException ignored) {}
	}
	return data;
    }

    private static Gson getGson() {
	if(gson == null) {
	    GsonBuilder builder = new GsonBuilder();
	    builder.registerTypeAdapter(Inspiration.Data.class, new Inspiration.Data.DataAdapter().nullSafe());
	    builder.registerTypeAdapter(FoodInfo.Data.class, new FoodInfo.Data.DataAdapter().nullSafe());
	    builder.registerTypeAdapter(GobbleInfo.Data.class, new GobbleInfo.Data.DataAdapter().nullSafe());
	    builder.registerTypeAdapter(ArtificeData.class, new ArtificeData.DataAdapter().nullSafe());
	    builder.setPrettyPrinting();
	    gson =  builder.create();
	}
	return gson;
    }
}

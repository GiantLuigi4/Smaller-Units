#flwbuiltins
#flwinclude <"flywheel:core/matutils.glsl">
#flwinclude <"flywheel:core/diffuse.glsl">

#[VertexData]
struct Vertex {
	vec3 pos;
	vec4 color;
	vec2 texCoords;
	vec2 modelLight;
	vec3 normal;
};

#flwinclude <"flywheel:data/blockfragment.glsl">

BlockFrag FLWMain(Vertex v) {
	vec4 worldPos = vec4(v.pos, 1.);
	vec3 norm = v.normal;
	
	FLWFinalizeWorldPos(worldPos);
	FLWFinalizeNormal(norm);
	
	BlockFrag b;
	b.diffuse = diffuse(norm);
	b.texCoords = v.texCoords;
	b.light = v.modelLight;
	
	#if defined(DEBUG_NORMAL)
	b.color = vec4(norm, 1.);
	#else
	b.color = vec4(v.color.xyz / diffuse(v.normal), v.color.a);
	#endif
	
	return b;
}
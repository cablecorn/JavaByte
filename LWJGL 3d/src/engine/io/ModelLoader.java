package engine.io;
import java.nio.IntBuffer;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

import engine.graphics.Material;
import engine.graphics.Mesh;
import engine.graphics.Vertex;
import engine.maths.Vector2f;
import engine.maths.Vector3f;




// Imports from the multi-texture and glass see-through work. BufferedImage,
// ImageIO, Map, and TreeMap are not used by the code below right now, but
// they are kept in case the glass handling needs them again.
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;







/*
 * MODEL LOADER — reads a 3D model file (such as .gltf) using Assimp and
 * converts it into the engine's own Mesh objects, so the Renderer can draw
 * it. Assimp is a library that understands many model file formats and gives
 * back one common structure: a scene that holds meshes and materials.
 *
 * Two versions of loadModel exist in this file:
 *   OLD (commented out below) — loads only the FIRST mesh in the file and
 *     gives it one texture that the caller passes in.
 *   NEW (active) — loads EVERY mesh in the file, finds each mesh's own
 *     texture from its material, and marks the glass mesh so it can be drawn
 *     see-through. This is what lets a model with many textures render.
 */
public class ModelLoader {

	/*
	 * GLASS MATERIAL — the material index that the model uses for its glass.
	 * Any mesh using this material index is flagged as glass in loadModel().
	 * This number is specific to the current model file, so a different model
	 * would need a different value.
	 */
	private static final int GLASS_MATERIAL = 7;

	
	
	
	
	/*
	 * OLD VERSION — SINGLE MESH LOADER (kept for comparison).
	 *
	 * Differences from the new version:
	 *  - Returns ONE Mesh and only reads scene.mMeshes().get(0), so every
	 *    other mesh in the model is ignored.
	 *  - Takes a texturePath from the caller and gives that one texture to
	 *    the mesh, so it cannot use the model's own per-material textures.
	 *  - Imports with only JoinIdenticalVertices and Triangulate. The new
	 *    version also asks Assimp to generate normals and to apply the
	 *    model's transforms (GenNormals and PreTransformVertices).
	 *  - Reads normals and UVs directly with no null check, and checks
	 *    mNumUVComponents() to decide whether the mesh has UVs. The new
	 *    version checks whether the buffers are null instead.
	 *  - Prints debug lines listing each mesh, its material, and its vertex
	 *    count.
	 *  - The closing brace of the class is also commented out here, because
	 *    this method was the last one in the old file.
	 */
//	public static Mesh loadModel(String filePath, String texturePath) {
//		AIScene scene = Assimp.aiImportFile(filePath, Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_Triangulate);
//		
////		if (scene == null) System.err.println("Couldn't load model at " + filePath);
//		
//		if (scene == null) {
//		    System.err.println("Couldn't load model at " + filePath + ": " + Assimp.aiGetErrorString());
//		    return null;
//		}
//
//		System.out.println("Meshes: " + scene.mNumMeshes());
//		System.out.println("Materials: " + scene.mNumMaterials());
//		for (int m = 0; m < scene.mNumMeshes(); m++) {
//		    AIMesh debugMesh = AIMesh.create(scene.mMeshes().get(m));
//		    System.out.println("Mesh " + m + " -> material " + debugMesh.mMaterialIndex() + ", vertices " + debugMesh.mNumVertices());
//		}
//		
//		AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));
//		int vertexCount = mesh.mNumVertices();
//		
//		AIVector3D.Buffer vertices = mesh.mVertices();
//		AIVector3D.Buffer normals = mesh.mNormals();
//		
//		Vertex[] vertexList = new Vertex[vertexCount];
//		
//		for (int i = 0; i < vertexCount; i++) {
//			AIVector3D vertex = vertices.get(i);
//			Vector3f meshVertex = new Vector3f(vertex.x(), vertex.y(), vertex.z());
//			
//			AIVector3D normal = normals.get(i);
//			Vector3f meshNormal = new Vector3f(normal.x(), normal.y(), normal.z());
//			
//			Vector2f meshTextureCoord = new Vector2f(0, 0);
//			if (mesh.mNumUVComponents().get(0) != 0) {
//				AIVector3D texture = mesh.mTextureCoords(0).get(i);
//				meshTextureCoord.setX(texture.x());
//				meshTextureCoord.setY(texture.y());
//			}
//			
//			vertexList[i] = new Vertex(meshVertex, meshNormal, meshTextureCoord);
//		}
//		
//		int faceCount = mesh.mNumFaces();
//		AIFace.Buffer indices = mesh.mFaces();
//		int[] indicesList = new int[faceCount * 3];
//		
//		for (int i = 0; i < faceCount; i++) {
//			AIFace face = indices.get(i);
//			indicesList[i * 3 + 0] = face.mIndices().get(0);
//			indicesList[i * 3 + 1] = face.mIndices().get(1);
//			indicesList[i * 3 + 2] = face.mIndices().get(2);
//		}
//		
//		return new Mesh(vertexList, indicesList, new Material(texturePath));
//	}
//}
//
//
//
//












	/*
	 * NEW VERSION — MULTI MESH, MULTI TEXTURE LOADER (active).
	 *
	 * Takes the path to the model file and a fallback texture path, and
	 * returns an array with one Mesh for every mesh in the model. The
	 * fallback texture is only used when a mesh's material has no texture
	 * of its own. Returns null if the file can't be loaded.
	 *
	 * Steps:
	 *  1. Import the file with Assimp, using these processing flags:
	 *       JoinIdenticalVertices — merges duplicate vertices so the index
	 *         buffer can share them.
	 *       Triangulate — turns any polygon into triangles, which is the
	 *         only shape the renderer draws.
	 *       GenNormals — creates normals if the file doesn't have any.
	 *       PreTransformVertices — applies each node's transform directly to
	 *         its vertices, so every mesh is already in the right place and
	 *         the scene is flattened.
	 *  2. For each mesh, copy its vertices (position, normal, texture
	 *     coordinate) into the engine's Vertex objects.
	 *  3. Copy its faces into a flat index array, three indices per triangle.
	 *  4. Look up the mesh's material and find which texture file it uses.
	 *  5. Build the Mesh with that texture, flag it as glass if its material
	 *     is the glass one, and add it to the list.
	 */
	public static Mesh[] loadModel(String filePath, String fallbackTexture) {
		AIScene scene = Assimp.aiImportFile(filePath,
				Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_Triangulate
				| Assimp.aiProcess_GenNormals | Assimp.aiProcess_PreTransformVertices);

		if (scene == null) {
			System.err.println("Couldn't load model at " + filePath + ": " + Assimp.aiGetErrorString());
			return null;
		}

		int meshCount = scene.mNumMeshes();
		List<Mesh> meshList = new ArrayList<>();
		System.out.println("Meshes: " + meshCount + ", Materials: " + scene.mNumMaterials());

		for (int m = 0; m < meshCount; m++) {
			AIMesh mesh = AIMesh.create(scene.mMeshes().get(m));
			int vertexCount = mesh.mNumVertices();

			AIVector3D.Buffer vertices = mesh.mVertices();
			AIVector3D.Buffer normals = mesh.mNormals();
			AIVector3D.Buffer uvs = mesh.mTextureCoords(0);

			// Build one Vertex per point. Normals and UVs can be missing, so
			// each gets a safe default: normal (0, 1, 0) pointing up, and UV (0, 0).
			Vertex[] vertexList = new Vertex[vertexCount];
			for (int i = 0; i < vertexCount; i++) {
				AIVector3D v = vertices.get(i);
				Vector3f pos = new Vector3f(v.x(), v.y(), v.z());

				Vector3f nor = new Vector3f(0, 1, 0);
				if (normals != null) {
					AIVector3D n = normals.get(i);
					nor = new Vector3f(n.x(), n.y(), n.z());
				}

				Vector2f uv = new Vector2f(0, 0);
				if (uvs != null) {
					AIVector3D t = uvs.get(i);
					uv.setX(t.x());
					uv.setY(t.y());
				}

				vertexList[i] = new Vertex(pos, nor, uv);
			}

			// Flatten the faces into one index list. Every face is a triangle
			// (guaranteed by the Triangulate flag), so each one gives 3 indices.
			int faceCount = mesh.mNumFaces();
			AIFace.Buffer faces = mesh.mFaces();
			int[] indicesList = new int[faceCount * 3];
			for (int i = 0; i < faceCount; i++) {
				AIFace face = faces.get(i);
				indicesList[i * 3] = face.mIndices().get(0);
				indicesList[i * 3 + 1] = face.mIndices().get(1);
				indicesList[i * 3 + 2] = face.mIndices().get(2);
			}

			// Work out which texture this mesh's material uses.
			// Ask Assimp for the material's diffuse (base color) texture name.
			// The AIString is native memory, so it is freed right after use.
			String texturePath = fallbackTexture;
			int matIndex = mesh.mMaterialIndex();
			AIMaterial material = AIMaterial.create(scene.mMaterials().get(matIndex));
			AIString path = AIString.calloc();
			Assimp.aiGetMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, 0, path,
					(IntBuffer) null, null, null, null, null, null);
			String found = path.dataString();
			path.free();

			System.out.println("Mesh " + m + " -> material " + matIndex + ", texture '" + found + "'");

			// Two cases for the texture name Assimp gave back:
			//  - Starts with "*": the image is embedded inside the model file
			//    (Assimp names them *0, *1, and so on). The code looks for a
			//    file named gltf_embedded_<number>.png in /textures/, so the
			//    embedded images need to be saved there under that name.
			//  - Otherwise it is a file path: keep only the file name and look
			//    for it in /textures/.
			// If the name is empty, the fallback texture stays in use.
			if (found.startsWith("*")) {
				texturePath = "/textures/gltf_embedded_" + found.substring(1) + ".png";
			} else if (!found.isEmpty()) {
				String fileName = found.replace("\\", "/");
				fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
				texturePath = "/textures/" + fileName;
			}

			Mesh result = new Mesh(vertexList, indicesList, new Material(texturePath));
			if (matIndex == GLASS_MATERIAL) {
				result.setGlass(true); // Renderer draws this one last, see-through
			}
			meshList.add(result);
		}

		return meshList.toArray(new Mesh[0]);
	}
}

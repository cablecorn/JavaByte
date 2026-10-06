with import <nixpkgs> {};
mkShell {
	buildInputs = [ jdk21 gradle ];
	NIX_LD_LIBRARY_PATH = lib.makeLibraryPath [
		stdenv.cc.cc
		libGL
		libglvnd
		mesa
		libX11
		libXcursor
		libXrandr
		libXi
		libXxf86vm
		libdecor
	];
	NIX_LD = lib.fileContents "${stdenv.cc}/nix-support/dynamic-linker";

	# Inject hardware acceleration paths so LWJGL can talk to GPU
	shellHook = ''
		export LD_LIBRARY_PATH="${lib.makeLibraryPath [ libGL libglvnd ]}:$LD_LIBRARY_PATH"
		echo "Graphics-ready Nix dev-shell loaded successfully!"
	'';
}

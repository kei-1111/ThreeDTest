package com.example.threedtest

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gorisse.thomas.lifecycle.lifecycle
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.loadHdrIndirectLight
import io.github.sceneview.loaders.loadHdrSkybox
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.nodes.ModelNode
import kotlinx.coroutines.launch


class MainFragment: Fragment(R.layout.main_fragment_layout) {

    lateinit var sceneView: SceneView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sceneView = view.findViewById<SceneView>(R.id.sceneView).apply {
            setLifecycle(lifecycle)
        }

        lifecycleScope.launch() {
            val hdrFile = "environments/studio_small_09_2k.hdr"
            sceneView.loadHdrIndirectLight(hdrFile, specularFilter = true) {
                intensity(30_000f)
            }
            sceneView.loadHdrSkybox(hdrFile) {
                intensity(50_000f)
            }
            sceneView.setZOrderOnTop(false)
            val model = sceneView.modelLoader.loadModel("models/lifith_AR_ver3.glb")!!
            val modelNode = ModelNode(sceneView, model).apply {
                transform(
                    position = Position(z = -5.5f),
                    rotation = Rotation(y = 180f)
                )
                scaleToUnitsCube(2.0f)
                // TODO: Fix centerOrigin
                //centerOrigin(Position(x=-1.0f, y=-1.0f))
                playAnimation()
            }
            sceneView.addChildNode(modelNode)

            sceneView.setZOrderOnTop(false)
        }
    }
    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    override fun onDestroyView(){
        super.onDestroyView()
        childFragmentManager.findFragmentById(R.id.sceneView)?.let { fragment ->
            childFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
        sceneView.destroy()
    }
}
/*
 * Copyright 2023 Erfan Sn
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.threedtest


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.View
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.threedtest.databinding.MainFragmentLayoutBinding
import io.github.sceneview.Scene
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.loadHdrIndirectLight
import io.github.sceneview.loaders.loadHdrIndirectLightAsync
import io.github.sceneview.loaders.loadHdrSkybox
import io.github.sceneview.loaders.loadHdrSkyboxAsync
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.nodes.ModelNode
import io.github.sceneview.nodes.Node
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "screen1") {
                composable(route = "screen1") {
                    Screen1(
                        navHostController = navController,
                    )
                }
                composable(route = "screen2") {
                    Screen2(
                        navHostController = navController,
                    )
                }
            }
        }
    }
}


@Composable
fun XmlView() {
    val activity = LocalContext.current as? AppCompatActivity ?: return
    var container = FragmentContainerView(activity)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LaunchedEffect(Unit) {
            activity.supportFragmentManager.commit(allowStateLoss = true) {
                replace(container!!.id, MainFragment::class.java, null)
            }
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                FragmentContainerView(context).also { container ->
                    container.id = View.generateViewId()
                }.also {
                    container = it
                }
            },
        )
        DisposableEffect(LocalLifecycleOwner.current) {
            onDispose {
                activity.supportFragmentManager.findFragmentById(container.id)?.let { fragment ->
                    activity.supportFragmentManager
                        .beginTransaction()
                        .remove(activity.supportFragmentManager.findFragmentById(container.id)!!)
                        .commitAllowingStateLoss()
                }
            }
        }
    }
}

@Composable
fun XmlView2() {
    AndroidViewBinding(MainFragmentLayoutBinding::inflate)
}

@Composable
fun Screen1(navHostController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .semantics { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Screen 1")
        Button(
            onClick = {
                navHostController.navigate("screen2") {
                    popUpTo("screen2") { inclusive = true }
                }
            }
        ) {
            Text(text = "Go to Screen 2")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun Screen2(
    navHostController: NavController
) {
    val nodes = remember { mutableStateListOf<Node>() }
    val lifecycleOwner = LocalLifecycleOwner.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {},
        contentAlignment = Alignment.Center
    ) {
        ModelScreen(lifecycleOwner)
        Text(text = "Screen 2")
        Button(onClick = {
            navHostController.navigate("screen1") {
                popUpTo("screen1") { inclusive = true }
            }
        }
        ) {
            Text(text = "Back to Screen 1")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ModelScreen(
    lifecycleOwner: LifecycleOwner
) {
    val nodes = remember { mutableStateListOf<Node>() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            nodes = nodes,
            onCreate = { sceneView ->
                sceneView.setLifecycle(lifecycleOwner.lifecycle)
                lifecycleOwner.lifecycle.coroutineScope.launch() {
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
                }
            }
        )
    }
}

@Composable
fun ModelScreen2() {
    val nodes = remember { mutableStateListOf<Node>() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            nodes = nodes,
            onCreate = { sceneView ->
                val hdrFile = "environments/studio_small_09_2k.hdr"
                sceneView.loadHdrIndirectLightAsync(
                    fileLocation = hdrFile,
                    specularFilter = true,
                    apply = { intensity(30_000f) }
                )
                sceneView.loadHdrSkyboxAsync(
                    fileLocation = hdrFile,
                    apply = { intensity(50_000f)}
                )
            }
        )
    }
}

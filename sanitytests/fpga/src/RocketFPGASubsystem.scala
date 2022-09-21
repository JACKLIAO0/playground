package sanitytests.fpga

import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.devices.debug.{HasPeripheryDebug, HasPeripheryDebugModuleImp}
import freechips.rocketchip.devices.tilelink.{BootROM, BootROMLocated}
import freechips.rocketchip.subsystem.{CBUS, ExtMem, RocketSubsystem, RocketSubsystemModuleImp}
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell.{DDRDesignInput, DDROverlayKey}
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._
import sifive.blocks.devices.gpio._

class RocketFPGASubsystem(resetWrangler: ClockAdapterNode, pll: PLLNode)(implicit p: Parameters)
    extends RocketSubsystem
    with HasPeripheryDebug
    with HasPeripheryUART
    with HasPeripheryGPIO
    with HasPeripherySPIFlash{
  override lazy val module = new RocketFPGASubSystemImp(this)
  val bootROM = p(BootROMLocated(location)).map { BootROM.attach(_, this, CBUS) }
  // attach DDR from shell, implicitly require ExtMem isDefined
  p(DDROverlayKey).headOption
    .map(
      _.place(DDRDesignInput(p(ExtMem).get.master.base, resetWrangler, pll))
    )
    .foreach { _.overlayOutput.ddr := mbus.toDRAMController(Some("xilinx_mig"))() }
}

class RocketFPGASubSystemImp(val out: RocketFPGASubsystem)
    extends RocketSubsystemModuleImp(out)
    with HasPeripheryDebugModuleImp
    with HasPeripherySPIFlashModuleImp
    with HasPeripheryUARTModuleImp
    with HasPeripheryGPIOModuleImp
